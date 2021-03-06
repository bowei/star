package org.star_lang.star.operators.arrays.runtime;

import org.star_lang.star.compiler.type.TypeUtils;
import org.star_lang.star.data.EvaluationException;
import org.star_lang.star.data.IArray;
import org.star_lang.star.data.IFunction;
import org.star_lang.star.data.IPattern;
import org.star_lang.star.data.IValue;
import org.star_lang.star.data.type.IType;
import org.star_lang.star.data.type.TypeVar;
import org.star_lang.star.data.type.UniversalType;
import org.star_lang.star.data.value.Array;
import org.star_lang.star.data.value.NTuple;
import org.star_lang.star.operators.CafeEnter;

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
public class ArraySequenceOps
{
  public static class ArrayNil implements IFunction
  {
    public static final String name = "__array_nil";

    @CafeEnter
    public static IValue enter() throws EvaluationException
    {
      return Array.nilArray;
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return enter();
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar tv = new TypeVar();
      return new UniversalType(tv, TypeUtils.functionType(TypeUtils.arrayType(tv)));
    }
  }

  public static class ArrayArray implements IFunction
  {
    public static final String name = "__array_array";

    @CafeEnter
    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return Array.newArray(args);
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar tv = new TypeVar();
      return new UniversalType(tv, TypeUtils.functionType(tv, TypeUtils.arrayType(tv)));
    }
  }

  public static class ArrayCons implements IFunction
  {
    public static final String name = "__array_cons";

    @CafeEnter
    public static IArray enter(IValue el, IArray array) throws EvaluationException
    {
      return array.consCell(el);
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return enter(args[0], (IArray) args[1]);
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar v = new TypeVar();
      IType arrayType = TypeUtils.arrayType(v);
      return new UniversalType(v, TypeUtils.functionType(v, arrayType, arrayType));
    }
  }

  public static class ArrayAppend implements IFunction
  {
    public static final String name = "__array_append";

    @CafeEnter
    public static IValue enter(IArray array, IValue el) throws EvaluationException
    {
      return array.addCell(el);
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return enter((IArray) args[0], args[1]);
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar v = new TypeVar();
      IType arrayType = TypeUtils.arrayType(v);
      return new UniversalType(v, TypeUtils.functionType(arrayType, v, TypeUtils.arrayType(v)));
    }
  }

  public static class UnaryArray implements IFunction
  {
    public static final String name = "__unary_array";

    @CafeEnter
    public static IValue enter(IValue el) throws EvaluationException
    {
      return Array.newArray(new IValue[] { el });
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return Array.newArray(args);
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar tv = new TypeVar();
      return new UniversalType(tv, TypeUtils.functionType(tv, TypeUtils.arrayType(tv)));
    }
  }

  public static class BinaryArray implements IFunction
  {
    public static final String name = "__binary_array";

    @CafeEnter
    public static IValue enter(IValue el1, IValue el2) throws EvaluationException
    {
      return Array.newArray(new IValue[] { el1, el2 });
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return Array.newArray(args);
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar tv = new TypeVar();
      return new UniversalType(tv, TypeUtils.functionType(tv, tv, TypeUtils.arrayType(tv)));
    }
  }

  public static class TernaryArray implements IFunction
  {
    public static final String name = "__ternary_array";

    @CafeEnter
    public static IValue enter(IValue el1, IValue el2, IValue el3) throws EvaluationException
    {
      return Array.newArray(new IValue[] { el1, el2, el3 });
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return Array.newArray(args);
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar tv = new TypeVar();
      return new UniversalType(tv, TypeUtils.functionType(tv, tv, tv, TypeUtils.arrayType(tv)));
    }
  }

  public static class ArrayEmptyMatch implements IPattern
  {
    public static final String name = "__array_empty_match";

    @CafeEnter
    public static IValue matches(IValue list) throws EvaluationException
    {
      IArray array = (IArray) list;
      if (array.isEmpty()) {
        return NTuple.$0Enum;
      } else
        return null;
    }

    @Override
    public IValue match(IValue list) throws EvaluationException
    {
      return matches(list);
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar tv = new TypeVar();
      return new UniversalType(tv, TypeUtils.patternType(TypeUtils.tupleType(), TypeUtils.arrayType(tv)));
    }
  }

  public static class ArrayHeadMatch implements IPattern
  {
    public static final String name = "__array_head_match";

    @CafeEnter
    public static IValue matches(IValue list) throws EvaluationException
    {
      Array array = (Array) list;

      if (array.size() >= 1)
        return NTuple.tuple(array.getCell(0), array.slice(1));
      else
        return null;
    }

    @Override
    public IValue match(IValue list) throws EvaluationException
    {
      return matches(list);
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar e = new TypeVar();
      IType l = TypeUtils.arrayType(e);

      return new UniversalType(e, TypeUtils.patternType(TypeUtils.tupleType(e, l), l));
    }
  }

  public static class ArrayTailMatch implements IPattern
  {
    public static final String name = "__array_tail_match";

    @CafeEnter
    public static IValue matches(IValue list) throws EvaluationException
    {
      Array array = (Array) list;

      int size = array.size();

      if (size >= 1)
        return NTuple.tuple(array.slice(0, size - 1), array.getCell(size - 1));
      else
        return null;
    }

    @Override
    public IValue match(IValue list) throws EvaluationException
    {
      return matches(list);
    }

    @Override
    public IType getType()
    {
      return type();
    }

    public static IType type()
    {
      TypeVar e = new TypeVar();
      IType l = TypeUtils.arrayType(e);

      return new UniversalType(e, TypeUtils.patternType(TypeUtils.tupleType(l, e), l));
    }
  }
}
