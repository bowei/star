package org.star_lang.star.compiler.canonical;

import org.star_lang.star.compiler.type.TypeUtils;
import org.star_lang.star.compiler.util.PrettyPrintDisplay;
import org.star_lang.star.data.type.IType;
import org.star_lang.star.data.type.Location;

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
public abstract class BaseExpression implements IContentExpression
{
  private final Location loc;
  private final IType type;

  protected BaseExpression(Location loc, IType type)
  {
    this.loc = loc;
    this.type = TypeUtils.deRef(type);
  }

  @Override
  public Location getLoc()
  {
    return loc;
  }

  @Override
  public IType getType()
  {
    return type;
  }

  @Override
  public String toString()
  {
    return PrettyPrintDisplay.toString(this);
  }
}
