package org.star_lang.star.compiler.cafe.compile;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.star_lang.star.code.repository.CodeCatalog;
import org.star_lang.star.code.repository.CodeRepository;
import org.star_lang.star.compiler.ErrorReport;
import org.star_lang.star.compiler.ast.Abstract;
import org.star_lang.star.compiler.ast.Apply;
import org.star_lang.star.compiler.ast.BigDecimalLiteral;
import org.star_lang.star.compiler.ast.BooleanLiteral;
import org.star_lang.star.compiler.ast.CharLiteral;
import org.star_lang.star.compiler.ast.FloatLiteral;
import org.star_lang.star.compiler.ast.IAbstract;
import org.star_lang.star.compiler.ast.IntegerLiteral;
import org.star_lang.star.compiler.ast.Literal;
import org.star_lang.star.compiler.ast.LongLiteral;
import org.star_lang.star.compiler.ast.Name;
import org.star_lang.star.compiler.ast.StringLiteral;
import org.star_lang.star.compiler.cafe.CafeSyntax;
import org.star_lang.star.compiler.cafe.Names;
import org.star_lang.star.compiler.cafe.compile.CaseCompile.ICaseCompile;
import org.star_lang.star.compiler.cafe.compile.Theta.IThetaBody;
import org.star_lang.star.compiler.cafe.compile.cont.CastConverter;
import org.star_lang.star.compiler.cafe.compile.cont.CheckCont;
import org.star_lang.star.compiler.cafe.compile.cont.ComboCont;
import org.star_lang.star.compiler.cafe.compile.cont.IContinuation;
import org.star_lang.star.compiler.cafe.compile.cont.JumpCont;
import org.star_lang.star.compiler.cafe.compile.cont.NonNullCont;
import org.star_lang.star.compiler.cafe.compile.cont.NullCont;
import org.star_lang.star.compiler.cafe.compile.cont.ReconcileCont;
import org.star_lang.star.compiler.cafe.type.CafeTypeDescription;
import org.star_lang.star.compiler.type.Freshen;
import org.star_lang.star.compiler.type.TypeUtils;
import org.star_lang.star.compiler.util.AccessMode;
import org.star_lang.star.compiler.util.StringUtils;
import org.star_lang.star.compiler.util.Wrapper;
import org.star_lang.star.data.EvaluationException;
import org.star_lang.star.data.IList;
import org.star_lang.star.data.IValue;
import org.star_lang.star.data.type.IType;
import org.star_lang.star.data.type.Location;
import org.star_lang.star.data.type.StandardTypes;
import org.star_lang.star.data.type.TypeExp;
import org.star_lang.star.data.type.TypeVar;
import org.star_lang.star.operators.ICafeBuiltin;
import org.star_lang.star.operators.Intrinsics;

/**
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * @author fgm
 * 
 */

public class Expressions
{
  public static final boolean CHECK_NONNULL = false;

  private final static Map<String, ICompileExpression> handlers = new HashMap<String, ICompileExpression>();
  private static final String SHALLOW_COPY = "shallowCopy";
  public static final String EQUALS = "equals";
  public static String EQUAL_SIG;

  private static String SHALLOW_SIG;
  public static String EVAL_EXCEPT_SIG;

  static {
    handlers.put(Names.FCALL, new CompileFunCall());
    handlers.put(Names.ESCAPE, new CompileEscape());
    handlers.put(Names.CONSTRUCT, new CompileConstructor());
    handlers.put(Names.RECORD, new CompileRecord());
    handlers.put(Names.FACE, new CompileFace());
    handlers.put(Names.COPY, new CompileCopy());
    handlers.put(Names.PERIOD, new CompileDot());
    handlers.put(Names.SWITCH, new CompileCase());
    handlers.put(Names.COLON, new CompileCast());
    handlers.put(Names.LET, new CompileLet());
    handlers.put(Names.ARROW, new CompileLambda());
    handlers.put(Names.PATTERN, new CompilePattern());
    handlers.put(Names.IF, new CompileConditional());
    handlers.put(Names.NOT, new CompileCondition());
    handlers.put(Names.OR, new CompileCondition());
    handlers.put(Names.AND, new CompileCondition());
    handlers.put(Names.MATCH, new CompileCondition());
    handlers.put(Names.THROW, new CompileThrow());
    handlers.put(Names.VALOF, new CompileValof());

    try {
      Method shallowMethod = IValue.class.getDeclaredMethod(SHALLOW_COPY);
      SHALLOW_SIG = org.objectweb.asm.Type.getMethodDescriptor(shallowMethod);

      Constructor<EvaluationException> evalExceptionCon = EvaluationException.class.getConstructor(IValue.class);
      EVAL_EXCEPT_SIG = org.objectweb.asm.Type.getConstructorDescriptor(evalExceptionCon);

      Method equalsMethod = Object.class.getDeclaredMethod(EQUALS, Object.class);
      EQUAL_SIG = org.objectweb.asm.Type.getMethodDescriptor(equalsMethod);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static ISpec compileExp(IAbstract exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer,
      String inFunction, IContinuation cont, Exit exit, CodeContext ccxt)
  {
    Location loc = exp.getLoc();
    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();
    InsnList ins = mtd.instructions;

    if (exp instanceof Apply) {
      ICompileExpression handler = handlers.get(((Apply) exp).getOp());
      if (handler != null)
        return handler.handleExp((Apply) exp, errors, dict, outer, inFunction, cont, exit, ccxt);
      else {
        errors.reportError("(internal) no expression handler for " + exp, loc);
        return SrcSpec.prcSrc;
      }
    } else if (CafeSyntax.isNullPtn(exp)) {
      ins.add(new InsnNode(Opcodes.ACONST_NULL));
      hwm.bump(1);
      return cont.cont(SrcSpec.prcSrc, dict, loc, errors, ccxt);
    } else if (exp instanceof Name)
      return handleName((Name) exp, errors, dict, outer, inFunction, cont, exit, ccxt);
    else if (exp instanceof BooleanLiteral) {
      ins.add(new InsnNode(((BooleanLiteral) exp).getLit() ? Opcodes.ICONST_1 : Opcodes.ICONST_0));
      hwm.bump(1);
      return cont.cont(SrcSpec.rawBoolSrc, dict, loc, errors, ccxt);
    } else if (exp instanceof IntegerLiteral) {
      int l = ((IntegerLiteral) exp).getLit();
      genIntConst(ins, hwm, l);
      return cont.cont(SrcSpec.rawIntSrc, dict, loc, errors, ccxt);
    } else if (exp instanceof LongLiteral) {
      genLongConst(ins, hwm, ((LongLiteral) exp).getLit());

      return cont.cont(SrcSpec.rawLngSrc, dict, loc, errors, ccxt);
    } else if (exp instanceof FloatLiteral) {
      double d = ((FloatLiteral) exp).getLit();

      genFloatConst(ins, hwm, d);
      return cont.cont(SrcSpec.rawDblSrc, dict, loc, errors, ccxt);
    } else if (exp instanceof BigDecimalLiteral) {
      BigDecimal big = ((BigDecimalLiteral) exp).getLit();
      genDecimalConst(ins, hwm, big);
      return cont.cont(SrcSpec.rawDecimalSrc, dict, loc, errors, ccxt);
    } else if (exp instanceof CharLiteral) {
      int ch = ((CharLiteral) exp).getLit();

      genIntConst(ins, hwm, ch);

      return cont.cont(SrcSpec.rawCharSrc, dict, loc, errors, ccxt);
    } else if (exp instanceof StringLiteral) {
      ins.add(new LdcInsnNode(((Literal) exp).getLit()));

      hwm.bump(1);
      return cont.cont(SrcSpec.rawStringSrc, dict, loc, errors, ccxt);
    } else {
      errors.reportError("invalid form of expression: " + exp, loc);
      return SrcSpec.prcSrc;
    }
  }

  public static void genIntConst(InsnList ins, HWM hwm, int ix)
  {
    switch (ix) {
    case -1:
      ins.add(new InsnNode(Opcodes.ICONST_M1));
      break;
    case 0:
      ins.add(new InsnNode(Opcodes.ICONST_0));
      break;
    case 1:
      ins.add(new InsnNode(Opcodes.ICONST_1));
      break;
    case 2:
      ins.add(new InsnNode(Opcodes.ICONST_2));
      break;
    case 3:
      ins.add(new InsnNode(Opcodes.ICONST_3));
      break;
    case 4:
      ins.add(new InsnNode(Opcodes.ICONST_4));
      break;
    case 5:
      ins.add(new InsnNode(Opcodes.ICONST_5));
      break;
    default:
      ins.add(new LdcInsnNode(ix));
      break;
    }
    hwm.bump(1);
  }

  public static void genLongConst(InsnList ins, HWM hwm, long ix)
  {
    if (ix == 0)
      ins.add(new InsnNode(Opcodes.LCONST_0));
    else if (ix == 1)
      ins.add(new InsnNode(Opcodes.LCONST_1));
    else
      ins.add(new LdcInsnNode(ix));

    hwm.bump(2);
  }

  public static void genFloatConst(InsnList ins, HWM hwm, double dx)
  {
    if (dx == 0.0)
      ins.add(new InsnNode(Opcodes.DCONST_0));
    else if (dx == 1.0)
      ins.add(new InsnNode(Opcodes.DCONST_1));
    else
      ins.add(new LdcInsnNode(dx));

    hwm.bump(2);
  }

  // We have to build a BigDecimal in small pieces
  public static void genDecimalConst(InsnList ins, HWM hwm, BigDecimal bx)
  {
    BigInteger bix = bx.unscaledValue();
    int scale = bx.scale();
    byte[] ixData = bix.toByteArray();
    int mark = hwm.bump(4);
    ins.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(BigDecimal.class)));
    ins.add(new InsnNode(Opcodes.DUP));
    ins.add(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(BigInteger.class)));
    ins.add(new InsnNode(Opcodes.DUP));
    genIntConst(ins, hwm, ixData.length);
    hwm.bump(1);
    ins.add(new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE));
    for (int ix = 0; ix < ixData.length; ix++) {
      int markb = hwm.bump(1);
      ins.add(new InsnNode(Opcodes.DUP));
      genIntConst(ins, hwm, ix);
      genIntConst(ins, hwm, ixData[ix]);
      ins.add(new InsnNode(Opcodes.BASTORE));
      hwm.reset(markb);
    }
    ins.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(BigInteger.class), Types.INIT, "([B)V"));
    genIntConst(ins, hwm, scale);
    ins.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(BigDecimal.class), Types.INIT, "("
        + Type.getDescriptor(BigInteger.class) + "I)V"));
    hwm.reset(mark);
    hwm.bump(1);
  }

  // An escape call looks like
  // fun(args...)
  private static class CompileEscape implements ICompileExpression
  {
    @Override
    public ISpec handleExp(Apply escape, ErrorReport errors, CafeDictionary dict, CafeDictionary outer,
        String inFunction, IContinuation cont, Exit exit, CodeContext ccxt)
    {
      assert CafeSyntax.isEscape(escape);
      String funName = CafeSyntax.escapeOp(escape);
      Location loc = escape.getLoc();
      VarInfo var = Theta.varReference(funName, dict, outer, loc, errors);
      MethodNode mtd = ccxt.getMtd();
      HWM hwm = ccxt.getMtdHwm();
      CodeCatalog bldCat = ccxt.getBldCat();

      Actions.doLineNumber(loc, mtd);

      if (var != null && var.getKind() == JavaKind.builtin) {
        InsnList ins = mtd.instructions;

        IType varType = Freshen.freshenForUse(var.getType());
        if (TypeUtils.isFunType(varType)) {
          int mark = hwm.getDepth();
          IList args = CafeSyntax.escapeArgs(escape);

          if (TypeUtils.arityOfFunctionType(varType) != args.size())
            errors.reportError("expecting " + TypeUtils.arityOfFunctionType(varType) + " arguments", loc);
          else {

            final ISpec[] argSpecs;

            ICafeBuiltin builtin = Intrinsics.getBuiltin(funName);

            if (builtin instanceof Inliner)
              ((Inliner) builtin).preamble(mtd, hwm);
            else if (!var.isStatic()) {
              hwm.bump(1);
              String javaName = escapeReference(var.getName(), dict, var.getJavaType(), var.getJavaSig(), errors);

              ins.add(new FieldInsnNode(Opcodes.GETSTATIC, escapeOwner(var.getName(), dict), javaName, var.getJavaSig()));
            }
            if (var.getJavaInvokeSig().equals(IFuncImplementation.IFUNCTION_INVOKE_SIG)) {
              argSpecs = SrcSpec.generics(varType, dict, bldCat, ccxt.getRepository(), errors, loc);

              Expressions.argArray(args, argSpecs, errors, dict, outer, inFunction, exit, ccxt);
            } else {
              argSpecs = SrcSpec.typeSpecs(var.getJavaInvokeSig(), dict, bldCat, errors, loc);

              compileArgs(args, argSpecs, errors, dict, outer, inFunction, exit, ccxt);
            }
            // actually invoke the function

            if (builtin instanceof Inliner)
              ((Inliner) builtin).inline(dict.getOwner(), mtd, hwm, loc);
            else if (var.isStatic()) {
              String funSig = var.getJavaInvokeSig();
              String classSig = var.getJavaType();

              ins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classSig, var.getJavaInvokeName(), funSig));
            } else if (var.getJavaInvokeSig().equals(IFuncImplementation.IFUNCTION_INVOKE_SIG)) {
              ins.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Types.IFUNC, Names.ENTERFUNCTION,
                  IFuncImplementation.IFUNCTION_INVOKE_SIG));
            } else {
              String methodName = var.getJavaInvokeName();
              String funSig = var.getJavaInvokeSig();
              String classSig = var.getJavaType();

              ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classSig, methodName, funSig));
            }
            hwm.reset(mark);

            ISpec resltSpec = argSpecs[argSpecs.length - 1];
            hwm.bump(Types.stackAmnt(Types.varType(resltSpec.getType())));

            return cont.cont(resltSpec, dict, loc, errors, ccxt);
          }
        } else
          errors.reportError("tried to invoke non-function: " + funName + ":" + varType, loc);
        return SrcSpec.prcSrc;
      } else
        errors.reportError(funName + " not an escape", escape.getLoc());
      return SrcSpec.prcSrc;
    }
  }

  // A function call looks like
  // fun(args...)
  private static class CompileFunCall implements ICompileExpression
  {
    @Override
    public ISpec handleExp(Apply app, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      Location loc = app.getLoc();
      if (CafeSyntax.isFunCall(app)) {
        String funName = CafeSyntax.funCallName(app);
        VarInfo var = Theta.varReference(funName, dict, outer, loc, errors);
        MethodNode mtd = ccxt.getMtd();

        Actions.doLineNumber(loc, mtd);

        if (var != null) {
          switch (var.getKind()) {
          case builtin:
            return invokeEscape(loc, var, app, errors, dict, outer, inFunction, cont, exit, ccxt);
          case constructor:
            return Constructors.constructorCall(loc, var, CafeSyntax.funCallArgs(app), errors, dict, outer, inFunction,
                cont, exit, ccxt);
          case general:
            return compileFunCall(loc, var, CafeSyntax.funCallArgs(app), errors, dict, outer, inFunction, cont, exit,
                ccxt);
          default:
            errors.reportError(var.getName() + " is not a function", loc);
          }
        } else
          errors.reportError(funName + " not declared", loc);
      } else
        errors.reportError("expecting a function call", loc);
      return SrcSpec.prcSrc;
    }
  }

  public static ISpec compileFunCall(Location loc, VarInfo var, IAbstract args, ErrorReport errors,
      CafeDictionary dict, CafeDictionary outer, String inFunction, IContinuation cont, Exit exit, CodeContext ccxt)
  {
    IType varType = Freshen.freshenForUse(var.getType());
    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();
    CodeCatalog bldCat = ccxt.getBldCat();

    InsnList ins = mtd.instructions;
    String funName = var.getName();

    ISpec[] argSpecs = SrcSpec.generics(varType, dict, bldCat, ccxt.getRepository(), errors, loc);

    int mark = hwm.bump(1);

    // preamble to access the appropriate value
    if (funName.equals(inFunction))
      ins.add(new VarInsnNode(Opcodes.ALOAD, dict.find(Names.PRIVATE_THIS).getOffset()));
    else
      var.loadValue(mtd, hwm, dict);

    ISpec funSpec = SrcSpec.generic(loc, var.getType(), dict, ccxt.getRepository(), errors);
    String methodType = funSpec.getJavaType();

    if (!methodType.equals(var.getJavaType()))
      ins.add(new TypeInsnNode(Opcodes.CHECKCAST, methodType));

    int arity = compileArgs(args, argSpecs, errors, dict, outer, inFunction, exit, ccxt);

    // actually invoke the function

    Actions.doLineNumber(loc, mtd);

    if (arity >= 0) {
      // This is awful, but the JVM made me do it.
      if (methodType.startsWith(Types.FUN_PREFIX) || methodType.startsWith(Types.PRC_PREFIX))
        ins.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, methodType, Names.ENTER, var.getJavaInvokeSig()));
      else
        ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, methodType, Names.ENTER, var.getJavaInvokeSig()));
    } else {
      ins.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Types.IFUNC, Names.ENTERFUNCTION,
          IFuncImplementation.IFUNCTION_INVOKE_SIG));
    }

    hwm.reset(mark);
    ISpec resltSpec = argSpecs[argSpecs.length - 1];
    hwm.bump(Types.stackAmnt(Types.varType(resltSpec.getType())));
    return cont.cont(resltSpec, dict, loc, errors, ccxt);
  }

  // A constructor call looks like a function call
  // fun(args...)
  private static class CompileConstructor implements ICompileExpression
  {
    @Override
    public ISpec handleExp(Apply app, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      if (CafeSyntax.isTuple(app))
        return Constructors.buildTuple(app.getLoc(), app, errors, dict, outer, inFunction, cont, exit, ccxt);
      else if (CafeSyntax.isConstructor(app)) {
        String funName = CafeSyntax.constructorOp(app);
        Location loc = app.getLoc();
        VarInfo var = Theta.varReference(funName, dict, outer, loc, errors);

        if (var != null) {
          switch (var.getKind()) {
          case constructor:
            return Constructors.constructorCall(loc, var, app, errors, dict, outer, inFunction, cont, exit, ccxt);
          case general:
            return Constructors.conFunCall(loc, var, app, errors, dict, outer, inFunction, cont, exit, ccxt);
          default:
            errors.reportError(funName + " not a constructor", loc);
          }
        } else
          errors.reportError(StringUtils.msg(funName, " not defined, expecting a constructor"), app.getLoc());
      } else
        errors.reportError("expecting a constructor", app.getLoc());
      return SrcSpec.prcSrc;
    }
  }

  // A constructor call looks like a function call
  // fun(args...)
  private static class CompileRecord implements ICompileExpression
  {
    @Override
    public ISpec handleExp(Apply app, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      assert CafeSyntax.isRecord(app);
      String funName = CafeSyntax.recordLabel(app);
      Location loc = app.getLoc();
      VarInfo var = Theta.varReference(funName, dict, outer, loc, errors);

      if (var != null) {
        switch (var.getKind()) {
        case constructor:
          return Constructors.recordCall(loc, var, app, errors, dict, outer, inFunction, cont, exit, ccxt);
        case general:
          return Constructors.recordFunCall(loc, var, app, errors, dict, outer, inFunction, cont, exit, ccxt);
        default:
          errors.reportError(funName + " not a record constructor", loc);
        }
      } else
        errors.reportError(StringUtils.msg(funName, " not defined, expecting a record constructor"), app.getLoc());
      return SrcSpec.prcSrc;
    }
  }

  private static class CompileFace implements ICompileExpression
  {
    @Override
    public ISpec handleExp(Apply exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      assert CafeSyntax.isFace(exp);

      return Faces.buildRecord(exp, errors, dict, outer, inFunction, cont, exit, ccxt);
    }

  }

  public static int compileRecordArgs(IList args, ISpec[] argSpecs, ErrorReport errors, CafeDictionary dict,
      CafeDictionary outer, String inFunction, Exit exit, CodeContext ccxt)
  {
    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();
    CodeCatalog bldCat = ccxt.getBldCat();

    if (args.size() < Theta.MAX_ARGS) {
      for (int ix = 0; ix < args.size(); ix++) {
        IAbstract arg = (IAbstract) args.getCell(ix);

        LabelNode nxLbl = new LabelNode();
        JumpCont argCont = new JumpCont(nxLbl);

        recordField(argSpecs, errors, dict, outer, inFunction, exit, ccxt, mtd, hwm, bldCat, ix, arg, nxLbl, argCont);
      }
      return args.size();
    } else {
      int arity = args.size();

      InsnList ins = mtd.instructions;

      genIntConst(ins, hwm, arity);
      ins.add(new TypeInsnNode(Opcodes.ANEWARRAY, Types.IVALUE));

      for (int ix = 0; ix < arity; ix++) {
        IAbstract arg = (IAbstract) args.getCell(ix);

        LabelNode nxLbl = new LabelNode();
        int mark = hwm.bump(1);
        ins.add(new InsnNode(Opcodes.DUP));
        genIntConst(ins, hwm, ix);

        IContinuation argCont = new JumpCont(nxLbl);
        if (CHECK_NONNULL)
          argCont = new ComboCont(new NonNullCont(), argCont);

        recordField(argSpecs, errors, dict, outer, inFunction, exit, ccxt, mtd, hwm, bldCat, ix, arg, nxLbl, argCont);

        ins.add(new InsnNode(Opcodes.AASTORE));
        hwm.reset(mark);
      }
      return -1;
    }
  }

  private static void recordField(ISpec[] argSpecs, ErrorReport errors, CafeDictionary dict, CafeDictionary outer,
      String inFunction, Exit exit, CodeContext ccxt, MethodNode mtd, HWM hwm, CodeCatalog bldCat, int ix,
      IAbstract arg, LabelNode nxLbl, IContinuation cont)
  {
    assert CafeSyntax.isField(arg);
    ISpec actual = compileExp(CafeSyntax.fieldValue(arg), errors, dict, outer, inFunction, cont, exit, ccxt);
    Utils.jumpTarget(mtd.instructions, nxLbl);
    checkType(actual, argSpecs[ix], mtd, dict, hwm, arg.getLoc(), errors, bldCat);
  }

  public static int compileArgs(IList args, ISpec[] argSpecs, ErrorReport errors, CafeDictionary dict,
      CafeDictionary outer, String inFunction, Exit exit, CodeContext ccxt)
  {
    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();
    CodeCatalog bldCat = ccxt.getBldCat();

    if (args.size() < Theta.MAX_ARGS) {
      for (int ix = 0; ix < args.size(); ix++) {
        IAbstract arg = (IAbstract) args.getCell(ix);

        LabelNode nxLbl = new LabelNode();

        ISpec actual = compileExp(arg, errors, dict, outer, inFunction, new JumpCont(nxLbl), exit, ccxt);
        Utils.jumpTarget(mtd.instructions, nxLbl);
        checkType(actual, argSpecs[ix], mtd, dict, hwm, arg.getLoc(), errors, bldCat);
      }
      return args.size();
    } else
      return argArray(args, argSpecs, errors, dict, outer, inFunction, exit, ccxt);
  }

  public static int compileArgs(IAbstract argTpl, ISpec[] argSpecs, ErrorReport errors, CafeDictionary dict,
      CafeDictionary outer, String inFunction, Exit exit, CodeContext ccxt)
  {
    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();
    CodeCatalog bldCat = ccxt.getBldCat();

    if (CafeSyntax.isConstructor(argTpl)) {
      IList args = CafeSyntax.constructorArgs(argTpl);
      if (argTpl.size() < Theta.MAX_ARGS) {
        for (int ix = 0; ix < args.size(); ix++) {
          IAbstract arg = (IAbstract) args.getCell(ix);

          LabelNode nxLbl = new LabelNode();

          IContinuation argCont = new JumpCont(nxLbl);
          if (CHECK_NONNULL)
            argCont = new ComboCont(new NonNullCont(), argCont);
          ISpec actual = compileExp(arg, errors, dict, outer, inFunction, argCont, exit, ccxt);
          Utils.jumpTarget(mtd.instructions, nxLbl);
          checkType(actual, argSpecs[ix], mtd, dict, hwm, arg.getLoc(), errors, bldCat);
        }
        return argTpl.size();
      } else
        return argArray(args, argSpecs, errors, dict, outer, inFunction, exit, ccxt);
    } else {
      LabelNode nxLbl = new LabelNode();
      compileExp(argTpl, errors, dict, outer, inFunction, new JumpCont(nxLbl), exit, ccxt);
      Utils.jumpTarget(mtd.instructions, nxLbl);

      mtd.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Types.ICONSTRUCTOR, Constructors.GET_CELLS,
          Constructors.GETCELLS_INVOKESIG));

      return -1;
    }
  }

  /**
   * @param ccxt
   *          TODO
   */
  public static boolean compileArgsToFrame(List<IAbstract> args, ISpec[] argSpecs, ErrorReport errors,
      CodeRepository repository, CodeCatalog bldCat, CafeDictionary dict, CafeDictionary outer, String inFunction,
      Exit exit, VarInfo[] vars, CodeContext ccxt)
  {
    if (args.size() >= Theta.MAX_ARGS || args.size() <= 3)
      return false;

    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();

    for (int ix = 0; ix < args.size(); ix++) {
      IAbstract arg = args.get(ix);

      LabelNode nxLbl = new LabelNode();
      ISpec actual = compileExp(arg, errors, dict, outer, inFunction, new JumpCont(nxLbl), exit, ccxt);
      String varName = "$field" + Integer.toString(ix);
      vars[ix] = dict.declareLocal(varName, argSpecs[ix], true, AccessMode.readOnly);
      Utils.jumpTarget(mtd.instructions, nxLbl);
      checkType(actual, argSpecs[ix], mtd, dict, hwm, arg.getLoc(), errors, bldCat);
      vars[ix].storeValue(mtd, hwm, dict);
    }
    return true;
  }

  /**
   * @param ccxt
   *          TODO
   */
  public static boolean compileArgsToFrame(IAbstract argTpl, ISpec[] argSpecs, ErrorReport errors,
      CodeRepository repository, CodeCatalog bldCat, CafeDictionary dict, CafeDictionary outer, String inFunction,
      Exit exit, VarInfo[] vars, CodeContext ccxt)
  {
    if (!CafeSyntax.isTuple(argTpl) || (argTpl.size() >= Theta.MAX_ARGS || argTpl.size() <= 3))
      return false;

    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();

    IList args = CafeSyntax.constructorArgs(argTpl);
    for (int ix = 0; ix < argTpl.size(); ix++) {
      IAbstract arg = (IAbstract) args.getCell(ix);

      LabelNode nxLbl = new LabelNode();

      ISpec actual = compileExp(arg, errors, dict, outer, inFunction, new JumpCont(nxLbl), exit, ccxt);
      String varName = "$field" + Integer.toString(ix);
      vars[ix] = dict.declareLocal(varName, argSpecs[ix], true, AccessMode.readOnly);
      Utils.jumpTarget(mtd.instructions, nxLbl);
      checkType(actual, argSpecs[ix], mtd, dict, hwm, arg.getLoc(), errors, bldCat);
      vars[ix].storeValue(mtd, hwm, dict);
    }
    return true;
  }

  private static class CompileCopy implements ICompileExpression
  {
    @Override
    public ISpec handleExp(Apply exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      Location loc = exp.getLoc();
      assert CafeSyntax.isCopy(exp);

      MethodNode mtd = ccxt.getMtd();
      HWM hwm = ccxt.getMtdHwm();
      InsnList ins = mtd.instructions;
      int mark = hwm.getDepth();

      IAbstract left = CafeSyntax.copied(exp);

      LabelNode nxLabel = new LabelNode();
      ISpec lftSrc = compileExp(left, errors, dict, outer, inFunction, new JumpCont(nxLabel), exit, ccxt);
      Utils.jumpTarget(ins, nxLabel);

      ins.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Types.IVALUE, SHALLOW_COPY, SHALLOW_SIG));
      ins.add(new TypeInsnNode(Opcodes.CHECKCAST, lftSrc.getJavaType()));

      hwm.reset(mark);
      hwm.bump(1);
      return cont.cont(lftSrc, dict, loc, errors, ccxt);
    }
  }

  public static ISpec invokeEscape(Location loc, VarInfo var, IAbstract call, ErrorReport errors, CafeDictionary dict,
      CafeDictionary outer, String inFunction, IContinuation cont, Exit exit, CodeContext ccxt)
  {
    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();
    CodeCatalog bldCat = ccxt.getBldCat();

    InsnList ins = mtd.instructions;
    String funName = var.getName();

    IType varType = Freshen.freshenForUse(var.getType());
    if (TypeUtils.isFunType(varType)) {
      int mark = hwm.getDepth();
      IAbstract args = CafeSyntax.funCallArgs(call);

      // preamble to access the appropriate value
      assert var.getKind() == JavaKind.builtin;

      final ISpec[] argSpecs;

      ICafeBuiltin builtin = Intrinsics.getBuiltin(funName);

      if (builtin instanceof Inliner)
        ((Inliner) builtin).preamble(mtd, hwm);
      else if (!var.isStatic()) {
        hwm.bump(1);
        String javaName = escapeReference(funName, dict, var.getJavaType(), var.getJavaSig(), errors);

        ins.add(new FieldInsnNode(Opcodes.GETSTATIC, escapeOwner(funName, dict), javaName, var.getJavaSig()));
      }
      if (var.getJavaInvokeSig().equals(IFuncImplementation.IFUNCTION_INVOKE_SIG)) {
        argSpecs = SrcSpec.generics(varType, dict, bldCat, ccxt.getRepository(), errors, loc);

        if (CafeSyntax.isTuple(args))
          argArray(CafeSyntax.constructorArgs(args), argSpecs, errors, dict, outer, inFunction, exit, ccxt);
        else
          compileArgs(args, argSpecs, errors, dict, outer, inFunction, exit, ccxt);
      } else {
        argSpecs = SrcSpec.typeSpecs(var.getJavaInvokeSig(), dict, bldCat, errors, loc);

        compileArgs(args, argSpecs, errors, dict, outer, inFunction, exit, ccxt);
      }
      // actually invoke the function

      if (builtin instanceof Inliner)
        ((Inliner) builtin).inline(dict.getOwner(), mtd, hwm, loc);
      else if (var.isStatic()) {
        String funSig = var.getJavaInvokeSig();
        String classSig = var.getJavaType();

        ins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classSig, var.getJavaInvokeName(), funSig));
      } else if (var.getJavaInvokeSig().equals(IFuncImplementation.IFUNCTION_INVOKE_SIG)) {
        ins.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Types.IFUNC, Names.ENTERFUNCTION,
            IFuncImplementation.IFUNCTION_INVOKE_SIG));
      } else {
        String methodName = var.getJavaInvokeName();
        String funSig = var.getJavaInvokeSig();
        String classSig = var.getJavaType();

        ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classSig, methodName, funSig));
      }
      hwm.reset(mark);

      ISpec resltSpec = argSpecs[argSpecs.length - 1];
      hwm.bump(Types.stackAmnt(Types.varType(resltSpec.getType())));

      return cont.cont(resltSpec, dict, loc, errors, ccxt);
    }
    return SrcSpec.prcSrc;
  }

  public static ISpec compileEscape(Location loc, VarInfo var, IAbstract call, ErrorReport errors,
      CodeRepository repository, CodeCatalog bldCat, CafeDictionary dict, CafeDictionary outer, String inFunction,
      IContinuation cont, Exit exit, CodeContext ccxt)
  {
    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();
    InsnList ins = mtd.instructions;
    String funName = var.getName();

    IType varType = Freshen.freshenForUse(var.getType());
    if (TypeUtils.isFunType(varType)) {
      int mark = hwm.getDepth();
      IList args = CafeSyntax.escapeArgs(call);

      if (TypeUtils.arityOfFunctionType(varType) != args.size())
        errors.reportError("expecting " + TypeUtils.arityOfFunctionType(varType) + " arguments", loc);
      else {
        // preamble to access the appropriate value
        assert var.getKind() == JavaKind.builtin;

        final ISpec[] argSpecs;

        ICafeBuiltin builtin = Intrinsics.getBuiltin(funName);

        if (builtin instanceof Inliner)
          ((Inliner) builtin).preamble(mtd, hwm);
        else if (!var.isStatic()) {
          hwm.bump(1);
          String javaName = escapeReference(funName, dict, var.getJavaType(), var.getJavaSig(), errors);

          ins.add(new FieldInsnNode(Opcodes.GETSTATIC, escapeOwner(funName, dict), javaName, var.getJavaSig()));
        }
        if (var.getJavaInvokeSig().equals(IFuncImplementation.IFUNCTION_INVOKE_SIG)) {
          argSpecs = SrcSpec.generics(varType, dict, bldCat, repository, errors, loc);

          Expressions.argArray(args, argSpecs, errors, dict, outer, inFunction, exit, ccxt);
        } else {
          argSpecs = SrcSpec.typeSpecs(var.getJavaInvokeSig(), dict, bldCat, errors, loc);

          compileArgs(args, argSpecs, errors, dict, outer, inFunction, exit, ccxt);
        }
        // actually invoke the function

        if (builtin instanceof Inliner)
          ((Inliner) builtin).inline(dict.getOwner(), mtd, hwm, loc);
        else if (var.isStatic()) {
          String funSig = var.getJavaInvokeSig();
          String classSig = var.getJavaType();

          ins.add(new MethodInsnNode(Opcodes.INVOKESTATIC, classSig, var.getJavaInvokeName(), funSig));
        } else if (var.getJavaInvokeSig().equals(IFuncImplementation.IFUNCTION_INVOKE_SIG)) {
          ins.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Types.IFUNC, Names.ENTERFUNCTION,
              IFuncImplementation.IFUNCTION_INVOKE_SIG));
        } else {
          String methodName = var.getJavaInvokeName();
          String funSig = var.getJavaInvokeSig();
          String classSig = var.getJavaType();

          ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, classSig, methodName, funSig));
        }
        hwm.reset(mark);

        ISpec resltSpec = argSpecs[argSpecs.length - 1];
        hwm.bump(Types.stackAmnt(Types.varType(resltSpec.getType())));

        return cont.cont(resltSpec, dict, loc, errors, ccxt);
      }
    } else
      errors.reportError("tried to invoke non-function: " + funName + ":" + varType, loc);
    return SrcSpec.prcSrc;
  }

  public static String escapeReference(String name, CafeDictionary dict, String javaType, String javaSig,
      ErrorReport errors)
  {
    String javaName = Utils.javaIdentifierOf(name);
    dict.addReference(javaName, new BuiltinInliner(javaName, javaType, javaSig, dict.getOwnerName()));
    return javaName;
  }

  public static String escapeOwner(String name, CafeDictionary dict)
  {
    String javaName = Utils.javaIdentifierOf(name);
    BuiltinInliner inliner = (BuiltinInliner) dict.getBuiltinReferences().get(javaName);
    assert inliner != null;
    return inliner.getJavaOwner();
  }

  public static class BuiltinInliner implements Inliner
  {
    private final String name;
    private final String javaOwner;
    private final String javaClass;
    private final String javaSig;

    BuiltinInliner(String name, String javaClass, String javaSig, String javaOwner)
    {
      this.name = name;
      this.javaClass = javaClass;
      this.javaSig = javaSig;
      this.javaOwner = javaOwner;
    }

    public String getJavaOwner()
    {
      return javaOwner;
    }

    @Override
    public void preamble(MethodNode mtd, HWM stackHWM)
    {
    }

    @Override
    public void inline(ClassNode klass, MethodNode mtd, HWM hwm, Location loc)
    {
      InsnList ins = new InsnList();
      assert klass.name.equals(javaOwner);

      int mark = hwm.bump(2);
      ins.add(new TypeInsnNode(Opcodes.NEW, javaClass));
      ins.add(new InsnNode(Opcodes.DUP));

      ins.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, javaClass, Types.INIT, "()V"));

      ins.add(new FieldInsnNode(Opcodes.PUTSTATIC, javaOwner, name, javaSig));
      hwm.reset(mark);

      Theta.addField(klass, name, javaSig, Opcodes.ACC_STATIC);

      hwm.reset(mark);
      mtd.instructions.insert(ins);
    }
  }

  public static boolean isStatic(VarInfo var)
  {
    return var.getWhere() == VarSource.staticMethod;
  }

  private static class CompileDot implements ICompileExpression
  {

    @Override
    public ISpec handleExp(Apply exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      assert CafeSyntax.isDot(exp);

      IAbstract record = CafeSyntax.dotRecord(exp);
      Location loc = exp.getLoc();
      if (!(record instanceof Name))
        errors.reportError("expecting an identifier", loc);

      String id = ((Name) record).getId();
      VarInfo var = Theta.varReference(id, dict, outer, loc, errors);
      if (var == null) {
        errors.reportError("(internal) " + id + " not found", loc);
        return SrcSpec.prcSrc;
      }
      IType recordType = TypeUtils.deRef(var.getType());
      IAbstract field = CafeSyntax.dotField(exp);
      final ISpec fieldSpec;
      MethodNode mtd = ccxt.getMtd();
      HWM hwm = ccxt.getMtdHwm();
      InsnList ins = mtd.instructions;
      LabelNode nxt = new LabelNode();
      compileExp(record, errors, dict, outer, inFunction, new CheckCont(nxt, var, dict), exit, ccxt);
      Utils.jumpTarget(ins, nxt);

      final IType fieldType;
      if (field instanceof Name) {
        String fieldName = Abstract.getId(field);
        if ((recordType instanceof TypeExp || recordType instanceof Type)
            && !TypeUtils.isAnonRecordLabel(recordType.typeLabel())) {
          fieldSpec = dict.getFieldSpec(recordType, fieldName);
          String getter = Types.getterName(dict.fieldJavaName(recordType, fieldName));
          String javaRecordType = dict.javaName(recordType);
          ins.add(new TypeInsnNode(Opcodes.CHECKCAST, javaRecordType));

          ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, javaRecordType, getter, "()"
              + dict.javaFieldSig(recordType, fieldName)));
          fieldType = fieldSpec.getType();
        } else {// We use the generic getMember method
          hwm.bump(1);
          ins.add(new TypeInsnNode(Opcodes.CHECKCAST, Types.IRECORD));
          ins.add(new LdcInsnNode(fieldName));
          ins.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, Types.IRECORD, "getMember", "(" + Types.JAVA_STRING_SIG
              + ")" + Types.IVALUE_SIG));
          fieldType = new TypeVar();
          fieldSpec = SrcSpec.generic(loc);
        }
      } else {
        int fieldOff = Abstract.getInt(field);
        genIntConst(ins, hwm, fieldOff);
        ins.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, var.getJavaType(), "getCell", "(I)" + Types.IVALUE_SIG));
        fieldType = new TypeVar();
        fieldSpec = SrcSpec.generic(loc);
      }

      switch (Types.varType(fieldType)) {
      case rawLong:
      case rawFloat:
        hwm.bump(1);
      default:
        ;
      }
      return cont.cont(fieldSpec, dict, loc, errors, ccxt);
    }
  }

  public static JavaKind arithType(IAbstract exp, CafeDictionary dict, CafeDictionary outer, ErrorReport errors)
  {
    if (exp instanceof IntegerLiteral)
      return JavaKind.rawInt;
    else if (exp instanceof LongLiteral)
      return JavaKind.rawLong;
    else if (exp instanceof FloatLiteral)
      return JavaKind.rawFloat;
    else if (exp instanceof CharLiteral)
      return JavaKind.rawChar;
    else if (exp instanceof StringLiteral)
      return JavaKind.rawString;
    else if (exp instanceof BigDecimalLiteral)
      return JavaKind.rawDecimal;
    else if (exp instanceof Name) {
      Name name = (Name) exp;
      Location loc = name.getLoc();
      VarInfo var = Theta.varReference(name.getId(), dict, outer, loc, errors);
      if (var != null)
        return var.getKind();
      else {
        errors.reportError(name.getId() + " not declared", loc);
        return JavaKind.general;
      }
    } else
      return JavaKind.general;
  }

  private static class CompileCast implements ICompileExpression
  {

    @Override
    public ISpec handleExp(Apply exp, final ErrorReport errors, final CafeDictionary dict, final CafeDictionary outer,
        String inFunction, IContinuation cont, Exit exit, CodeContext ccxt)
    {
      assert CafeSyntax.isTypedTerm(exp);
      CodeCatalog bldCat = ccxt.getBldCat();

      final IType castType = TypeAnalyser.parseType(CafeSyntax.typedType(exp), dict, errors);
      // we pretty much ignore a function type when casting to it...
      final ISpec castSpec = TypeUtils.isProgramType(castType) ? SrcSpec.generic(exp.getLoc(), castType, dict, ccxt
          .getRepository(), errors) : SrcSpec.typeSpec(exp.getLoc(), castType, dict, bldCat, errors);

      final IAbstract term = CafeSyntax.typedTerm(exp);

      IContinuation converter = new CastConverter(castType, ccxt.getMtdHwm(), castSpec);

      IContinuation combo = new ComboCont(converter, cont);
      return compileExp(term, errors, dict, outer, inFunction, combo, exit, ccxt);
    }
  }

  // A case looks like switch Exp in { Ptn -> Exp; ... ; } else Exp
  private static class CompileCase implements ICompileExpression
  {

    @Override
    public ISpec handleExp(Apply exp, final ErrorReport errors, final CafeDictionary dict, final CafeDictionary outer,
        final String inFunction, final IContinuation cont, final Exit exit, final CodeContext ccxt)
    {
      assert CafeSyntax.isSwitch(exp);
      MethodNode mtd = ccxt.getMtd();

      InsnList ins = mtd.instructions;
      LabelNode lbl = new LabelNode();
      ins.add(lbl);
      IAbstract sel = CafeSyntax.switchSel(exp);

      assert sel instanceof Name;
      String id = ((Name) sel).getId();
      Location loc = exp.getLoc();
      VarInfo var = Theta.varReference(id, dict, outer, loc, errors);
      assert var != null && var.isInited();

      ICaseCompile handler = new ICaseCompile() {

        @Override
        public ISpec compile(IAbstract term, CafeDictionary dict, IContinuation cont)
        {
          return compileExp(term, errors, dict, outer, inFunction, cont, exit, ccxt);
        }
      };
      return CaseCompile.compileSwitch(exp.getLoc(), var, CafeSyntax.switchCases(exp), CafeSyntax.switchDeflt(exp),
          dict, outer, errors, handler, cont, ccxt);
    }
  }

  // A let looks like let <defs> in <bound>
  private static class CompileLet implements ICompileExpression
  {
    @Override
    public ISpec handleExp(final Apply let, final ErrorReport errors, CafeDictionary dict, final CafeDictionary outer,
        final String inFunction, final IContinuation cont, final Exit exit, final CodeContext ccxt)
    {
      assert CafeSyntax.isLetExp(let);

      IList theta = CafeSyntax.letDefs(let);

      MethodNode mtd = ccxt.getMtd();
      LabelNode endLabel = new LabelNode();
      ISpec expType = Theta.compileDefinitions(theta, dict, outer, endLabel, inFunction, new LocalDefiner(endLabel,
          ccxt), new IThetaBody() {

        @Override
        public ISpec compile(CafeDictionary thetaDict, CodeCatalog bldCat, ErrorReport errors, CodeRepository repository)
        {
          return compileExp(CafeSyntax.letBound(let), errors, thetaDict, outer, inFunction, cont, exit, ccxt);
        }

        @Override
        public void introduceType(CafeTypeDescription type)
        {
        }
      }, let.getLoc(), errors, ccxt);
      mtd.instructions.add(endLabel);
      return expType;
    }
  }

  // A local, in-line function definition
  private static class CompileLambda implements ICompileExpression
  {
    @Override
    public ISpec handleExp(Apply exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      IType funType = Theta.computeLambdaType(exp, dict, errors);

      return cont.cont(Theta.compileLambda(exp, funType, errors, dict, outer, ccxt), dict, exp.getLoc(), errors, ccxt);
    }
  }

  private static class CompilePattern implements ICompileExpression
  {
    @Override
    public ISpec handleExp(Apply exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      return cont.cont(Theta.compilePattern(exp, errors, dict, ccxt), dict, exp.getLoc(), errors, ccxt);
    }
  }

  public static ISpec handleName(Name name, ErrorReport errors, CafeDictionary dict, CafeDictionary outer,
      String inFunction, IContinuation cont, Exit exit, CodeContext ccxt)
  {
    String id = name.getId();
    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();
    InsnList ins = mtd.instructions;
    Location loc = name.getLoc();
    VarInfo var = Theta.varReference(id, dict, outer, loc, errors);

    if (var != null) {
      if (!var.isInited())
        errors.reportError("accessing uninitiliazed variable: " + id + "\ndeclared at " + var.getLoc(), loc);
      else if (var.getWhere() == VarSource.staticMethod)
        errors.reportError("cannot treat static method " + id + " as a regular value", loc);
      else
        var.loadValue(mtd, hwm, dict);

      return cont.cont(var, dict, loc, errors, ccxt);
    } else if (CafeSyntax.isVoid(name)) {
      ins.add(new InsnNode(Opcodes.ACONST_NULL));
      hwm.bump(1);
      return cont.cont(SrcSpec.prcSrc, dict, loc, errors, ccxt);
    } else {
      errors.reportError(name + " not defined", loc);
      return cont.cont(SrcSpec.prcSrc, dict, loc, errors, ccxt);
    }
  }

  private static class CompileCondition implements ICompileExpression
  {

    @Override
    public ISpec handleExp(Apply exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      MethodNode mtd = ccxt.getMtd();
      HWM hwm = ccxt.getMtdHwm();
      InsnList ins = mtd.instructions;
      hwm.bump(1);
      LabelNode lf = new LabelNode();
      LabelNode lx = new LabelNode();

      Conditions.compileCond(exp, Sense.jmpOnFail, lf, errors, dict, outer, inFunction, exit, ccxt);
      ins.add(new InsnNode(Opcodes.ICONST_1));
      ins.add(new JumpInsnNode(Opcodes.GOTO, lx));
      ins.add(lf);
      ins.add(new InsnNode(Opcodes.ICONST_0));
      ins.add(lx);

      return cont.cont(SrcSpec.rawBoolSrc, dict, exp.getLoc(), errors, ccxt);
    }
  }

  // A conditional looks like:
  // if <test> then <then> else <else>
  private static class CompileConditional implements ICompileExpression
  {

    @Override
    public ISpec handleExp(Apply exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      assert CafeSyntax.isConditional(exp);

      IAbstract condition = CafeSyntax.conditionalTest(exp);
      IAbstract th = CafeSyntax.conditionalThen(exp);
      IAbstract el = CafeSyntax.conditionalElse(exp);
      LabelNode thLabel = new LabelNode();
      LabelNode elLabel = new LabelNode();
      MethodNode mtd = ccxt.getMtd();
      InsnList ins = mtd.instructions;

      ReconcileCont reconcile = new ReconcileCont(cont);

      Actions.doLineNumber(condition.getLoc(), mtd);

      CafeDictionary thDict = dict.fork();
      Conditions.compileCond(condition, Sense.jmpOnFail, elLabel, errors, thDict, outer, inFunction, exit, ccxt);
      Utils.jumpTarget(ins, thLabel);
      compileExp(th, errors, thDict, outer, inFunction, reconcile, exit, ccxt);
      dict.migrateFreeVars(thDict);
      ins.add(elLabel);
      CafeDictionary elDict = dict.fork();
      compileExp(el, errors, thDict, outer, inFunction, reconcile, exit, ccxt);
      dict.migrateFreeVars(elDict);
      return reconcile.getSpec();
    }
  }

  // A throw expression
  private static class CompileThrow implements ICompileExpression
  {

    @Override
    public ISpec handleExp(Apply exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        IContinuation cont, Exit exit, CodeContext ccxt)
    {
      assert CafeSyntax.isThrow(exp);

      MethodNode mtd = ccxt.getMtd();
      InsnList ins = mtd.instructions;

      compileExp(CafeSyntax.thrownExp(exp), errors, dict, outer, inFunction, new NullCont(), exit, ccxt);
      ins.add(new InsnNode(Opcodes.ATHROW));

      return cont.cont(SrcSpec.prcSrc, dict, exp.getLoc(), errors, ccxt);
    }
  }

  // A valof looks like:
  // valof <action>
  private static class CompileValof implements ICompileExpression
  {

    @Override
    public ISpec handleExp(Apply exp, ErrorReport errors, CafeDictionary dict, CafeDictionary outer, String inFunction,
        final IContinuation cont, Exit exit, CodeContext ccxt)
    {
      assert CafeSyntax.isValof(exp);
      final MethodNode mtd = ccxt.getMtd();
      final InsnList ins = mtd.instructions;
      final LabelNode endLbl = new LabelNode();
      final Wrapper<ISpec> resType = Wrapper.create(null);

      IContinuation valisCont = new IContinuation() {

        @Override
        public ISpec cont(ISpec src, CafeDictionary cxt, Location loc, ErrorReport errors, CodeContext ccxt)
        {
          resType.set(src);
          src = cont.cont(src, cxt, loc, errors, ccxt);
          if (!cont.isJump())
            mtd.instructions.add(new JumpInsnNode(Opcodes.GOTO, endLbl));
          return src;
        }

        @Override
        public boolean isJump()
        {
          return cont.isJump();
        }
      };

      Exit valof = new Exit(Names.VALIS, valisCont, exit);
      Actions.compileAction(CafeSyntax.valofAction(exp), errors, valof, dict, outer, endLbl, inFunction, new JumpCont(
          endLbl), ccxt);
      Utils.jumpTarget(ins, endLbl);

      if (resType.get() == null)
        return SrcSpec.prcSrc;

      return resType.get();
    }
  }

  // This is messy because of the weird conflict between pushing types through
  // the JVM and an equality based type
  // system.
  public static void checkType(ISpec actual, ISpec exp, MethodNode mtd, CafeDictionary dict, HWM hwm, Location loc,
      ErrorReport errors, CodeCatalog bldCat)
  {
    IType expectedType = exp.getType();
    IType actualType = actual.getType();

    if (actual.getJavaType().equals(exp.getJavaType()))
      return;
    else if (actualType.equals(TypeUtils.rawType(expectedType)))
      AutoBoxing.boxValue(actualType, mtd.instructions, dict);
    else if (expectedType.equals(TypeUtils.rawType(actualType)))
      AutoBoxing.unboxValue(mtd, hwm, expectedType);
    else if (TypeUtils.isRawBoolType(expectedType))
      AutoBoxing.unboxValue(mtd, hwm, expectedType);// special case for booleans
    else if (TypeUtils.isRawBoolType(actualType))
      AutoBoxing.boxValue(actualType, mtd.instructions, dict);
    else if (TypeUtils.isRawBinaryType(actualType))
      mtd.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, exp.getJavaType()));
    else if (TypeUtils.isRawBinaryType(expectedType)
        && (TypeUtils.isRawType(actualType) || TypeUtils.isType(actualType, StandardTypes.ANY)))
      mtd.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, exp.getJavaType()));
    else if (TypeUtils.isRawType(expectedType)
        && (TypeUtils.isTypeVar(actualType) || TypeUtils.isCookedType(actualType, expectedType)))
      AutoBoxing.unboxValue(mtd, hwm, expectedType);
    else if (TypeUtils.isRawType(actualType)
        && (TypeUtils.isTypeVar(expectedType) || TypeUtils.isCookedType(expectedType, actualType)))
      AutoBoxing.boxValue(actualType, mtd.instructions, dict);
    else if (!exp.getJavaType().equals(Types.IVALUE))
      mtd.instructions.add(new TypeInsnNode(Opcodes.CHECKCAST, exp.getJavaType()));
  }

  /**
   * Compile arg sequence using the varargs interface
   * 
   * @param args
   * @param argSpecs
   * @param errors
   * @param dict
   * @param outer
   * @param inFunction
   * @param exit
   * @param ccxt
   *          TODO
   */
  public static int argArray(IList args, ISpec[] argSpecs, ErrorReport errors, CafeDictionary dict,
      CafeDictionary outer, String inFunction, Exit exit, CodeContext ccxt)
  {
    int arity = args.size();

    MethodNode mtd = ccxt.getMtd();
    HWM hwm = ccxt.getMtdHwm();
    CodeCatalog bldCat = ccxt.getBldCat();

    InsnList ins = mtd.instructions;

    genIntConst(ins, hwm, arity);
    ins.add(new TypeInsnNode(Opcodes.ANEWARRAY, Types.IVALUE));

    for (int ix = 0; ix < arity; ix++) {
      IAbstract arg = (IAbstract) args.getCell(ix);

      LabelNode nxLbl = new LabelNode();
      int mark = hwm.bump(1);
      ins.add(new InsnNode(Opcodes.DUP));
      genIntConst(ins, hwm, ix);

      IContinuation argCont = new JumpCont(nxLbl);
      if (CHECK_NONNULL)
        argCont = new ComboCont(new NonNullCont(), argCont);

      ISpec actual = compileExp(arg, errors, dict, outer, inFunction, argCont, exit, ccxt);
      Utils.jumpTarget(mtd.instructions, nxLbl);
      checkType(actual, argSpecs[ix], mtd, dict, hwm, arg.getLoc(), errors, bldCat);
      ins.add(new InsnNode(Opcodes.AASTORE));
      hwm.reset(mark);
    }
    return -1;
  }
}
