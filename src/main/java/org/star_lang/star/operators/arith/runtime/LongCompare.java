package org.star_lang.star.operators.arith.runtime;

import org.star_lang.star.compiler.type.TypeUtils;
import org.star_lang.star.operators.CafeEnter;

import com.starview.platform.data.EvaluationException;
import com.starview.platform.data.IFunction;
import com.starview.platform.data.IValue;
import com.starview.platform.data.type.IType;
import com.starview.platform.data.type.StandardTypes;
import com.starview.platform.data.value.BoolWrap;
import com.starview.platform.data.value.Factory;

/**
 * 
 * Copyright (C) 2013 Starview Inc
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

public abstract class LongCompare
{
  private static final IType longType = StandardTypes.rawLongType;

  public static class LongEQ implements IFunction
  {
    @CafeEnter
    public static BoolWrap enter(long ix1, long ix2)
    {
      return Factory.newBool(ix1 == ix2);
    }

    @Override
    public IType getType()
    {
      return TypeUtils.functionType(longType, longType, StandardTypes.booleanType);
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return enter(Factory.lngValue(args[0]), Factory.lngValue(args[1]));
    }
  }

  public static class LongNE implements IFunction
  {
    @CafeEnter
    public static BoolWrap enter(long ix1, long ix2)
    {
      return Factory.newBool(ix1 != ix2);
    }

    @Override
    public IType getType()
    {
      return TypeUtils.functionType(longType, longType, StandardTypes.booleanType);
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return enter(Factory.lngValue(args[0]), Factory.lngValue(args[1]));
    }
  }

  public static class LongLE implements IFunction
  {
    @CafeEnter
    public static BoolWrap enter(long ix1, long ix2)
    {
      return Factory.newBool(ix1 <= ix2);
    }

    @Override
    public IType getType()
    {
      return TypeUtils.functionType(longType, longType, StandardTypes.booleanType);
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return enter(Factory.lngValue(args[0]), Factory.lngValue(args[1]));
    }
  }

  public static class LongLT implements IFunction
  {
    @CafeEnter
    public static BoolWrap enter(long ix1, long ix2)
    {
      return Factory.newBool(ix1 < ix2);
    }

    @Override
    public IType getType()
    {
      return TypeUtils.functionType(longType, longType, StandardTypes.booleanType);
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return enter(Factory.lngValue(args[0]), Factory.lngValue(args[1]));
    }
  }

  public static class LongGT implements IFunction
  {
    @CafeEnter
    public static BoolWrap enter(long ix1, long ix2)
    {
      return Factory.newBool(ix1 > ix2);
    }

    @Override
    public IType getType()
    {
      return TypeUtils.functionType(longType, longType, StandardTypes.booleanType);
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return enter(Factory.lngValue(args[0]), Factory.lngValue(args[1]));
    }
  }

  public static class LongGE implements IFunction
  {
    @CafeEnter
    public static BoolWrap enter(long ix1, long ix2)
    {
      return Factory.newBool(ix1 >= ix2);
    }

    @Override
    public IType getType()
    {
      return TypeUtils.functionType(longType, longType, StandardTypes.booleanType);
    }

    @Override
    public IValue enter(IValue... args) throws EvaluationException
    {
      return enter(Factory.lngValue(args[0]), Factory.lngValue(args[1]));
    }
  }
}
