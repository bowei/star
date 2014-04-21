package org.star_lang.star.compiler.canonical;

import com.starview.platform.data.type.IType;
import com.starview.platform.data.type.Location;

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
@SuppressWarnings("serial")
public abstract class ProgramLiteral extends BaseExpression
{
  protected final Variable freeVars[];
  protected final String name;

  protected ProgramLiteral(Location loc, IType type, String name, Variable[] freeVars)
  {
    super(loc, type);
    assert freeVars != null;
    this.freeVars = freeVars;
    this.name = name;
  }

  public Variable[] getFreeVars()
  {
    return freeVars;
  }

  public String getName()
  {
    return name;
  }
}