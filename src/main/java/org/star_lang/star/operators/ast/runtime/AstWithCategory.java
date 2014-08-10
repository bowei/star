package org.star_lang.star.operators.ast.runtime;

import org.star_lang.star.compiler.ast.ASyntax;
import org.star_lang.star.compiler.type.TypeUtils;
import org.star_lang.star.data.EvaluationException;
import org.star_lang.star.data.IFunction;
import org.star_lang.star.data.IValue;
import org.star_lang.star.data.type.IType;
import org.star_lang.star.data.type.StandardTypes;
import org.star_lang.star.data.value.StringWrap;
import org.star_lang.star.data.value.StringWrap.StringWrapper;
import org.star_lang.star.operators.CafeEnter;

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
public class AstWithCategory implements IFunction
{
  public static final String name = "__astWithCategory";

  @CafeEnter
  public static IValue enter(ASyntax term, StringWrap category) throws EvaluationException
  {
    term.setCategory(((StringWrapper) category).getValue());
    return term;
  }

  @Override
  public IValue enter(IValue... args) throws EvaluationException
  {
    return enter((ASyntax) args[0], (StringWrap) args[1]);
  }

  @Override
  public IType getType()
  {
    return type();
  }

  public static IType type()
  {
    return TypeUtils.functionType(ASyntax.type, StandardTypes.stringType, ASyntax.type);
  }
}