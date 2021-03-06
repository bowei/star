package org.star_lang.star.data.type;

import org.star_lang.star.compiler.operator.Operators;
import org.star_lang.star.compiler.standard.StandardNames;
import org.star_lang.star.compiler.type.Dictionary;
import org.star_lang.star.compiler.type.DisplayType;
import org.star_lang.star.compiler.type.Freshen;
import org.star_lang.star.compiler.type.Subsume;
import org.star_lang.star.compiler.type.TypeUtils;
import org.star_lang.star.compiler.util.PrettyPrintDisplay;
import org.star_lang.star.compiler.util.StringUtils;

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
@SuppressWarnings("serial")
public class TypeAlias implements ITypeAlias
{
  private final Location loc;
  private final IType alias;
  private final Type typeCon;

  private TypeAlias(Location loc, Type typeCon, IType alias)
  {
    this.loc = loc;
    this.typeCon = typeCon;
    this.alias = alias;
  }

  public TypeAlias(Location loc, IType alias)
  {
    this(loc, (Type) TypeUtils.getTypeCon(TypeUtils.getTypeArg(TypeUtils.unwrap(alias), 0)), alias);
  }

  public static TypeAlias typeAlias(Location loc, String label, IType bound)
  {
    return typeAlias(loc, label, Kind.type, bound);
  }

  public static TypeAlias typeAlias(Location loc, String label, Kind kind, IType bound)
  {
    Type typeCon = new Type(label, kind);
    IType alias = TypeUtils.typeExp(StandardNames.ALIAS, typeCon, bound);
    return new TypeAlias(loc, typeCon, alias);
  }

  public static TypeAlias typeAlias(Location loc, String label, int arity, IType bound)
  {
    assert bound.kind().mode() == Kind.Mode.typefunction;
    IType[] args = new IType[arity];
    for (int ix = 0; ix < arity; ix++)
      args[ix] = new TypeVar();
    Type typeCon = new Type(label, Kind.kind(arity));

    IType alias = TypeUtils.typeExp(StandardNames.ALIAS, new TypeExp(typeCon, args), new TypeExp(bound, args));
    return new TypeAlias(loc, typeCon, alias);
  }

  @Override
  public int typeArity()
  {
    return typeCon.getArity();
  }

  @Override
  public Type getType()
  {
    return typeCon;
  }

  @Override
  public Kind kind()
  {
    return typeCon.kind();
  }

  @Override
  public IType apply(IType tp, Location loc, Dictionary cxt) throws TypeConstraintException
  {
    tp = TypeUtils.deRef(tp);

    if (tp.typeLabel().equals(typeCon.typeLabel())) {
      IType refreshed = Freshen.freshenForUse(alias);
      assert TypeUtils.isType(refreshed, StandardNames.ALIAS, 2);
      IType lhs = TypeUtils.getTypeArg(refreshed, 0);

      // We unpack this here because the first thing subsume does is check aliases ...
      if (tp instanceof TypeExp && lhs instanceof TypeExp) {
        IType[] tpArgs = ((TypeExp) tp).getTypeArgs();
        IType[] lhsArgs = ((TypeExp) lhs).getTypeArgs();
        if (tpArgs.length == lhsArgs.length) {
          for (int ix = 0; ix < tpArgs.length; ix++)
            Subsume.subsume(tpArgs[ix], lhsArgs[ix], loc, cxt, true);
          return TypeUtils.checkAlias(TypeUtils.getTypeArg(refreshed, 1), cxt, loc);
        }
      } else if (tp instanceof Type && lhs instanceof Type)
        return TypeUtils.checkAlias(TypeUtils.getTypeArg(refreshed, 1), cxt, loc);
      else
        return tp;
    }
    throw new TypeConstraintException(StringUtils.msg("type ", tp, " not an instance of type alias ", alias), loc);
  }

  public IType alias()
  {
    return alias;
  }

  @Override
  public IType verifyType(IType type, Location loc, Dictionary dict) throws TypeConstraintException
  {
    return apply(type, loc, dict);
  }

  @Override
  public Location getLoc()
  {
    return loc;
  }

  @Override
  public String getName()
  {
    return typeCon.typeLabel();
  }

  @Override
  public void prettyPrint(PrettyPrintDisplay disp)
  {
    IType tp = TypeUtils.unwrap(alias);

    assert TypeUtils.isType(tp, StandardNames.ALIAS, 2);
    IType lhs = TypeUtils.getTypeArg(tp, 0);
    IType rhs = TypeUtils.getTypeArg(tp, 1);

    disp.appendWord(StandardNames.TYPE);
    DisplayType.display(disp, lhs);
    disp.append(" is alias of ");
    DisplayType.display(disp, rhs, Operators.OF_PRIORITY - 1);
  }

  @Override
  public String toString()
  {
    return PrettyPrintDisplay.toString(this);
  }
}
