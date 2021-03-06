package org.star_lang.star.data;

/**
 * This is a marker interface for scalars. In effect, an IValue becomes a wrapper for an arbitrary
 * Java object.
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
 * This wrapper is only used within Cafe/StarRules to support the getMember and getCell methods in
 * the IRecord and IArray interfaces.
 * 
 * @author fgm
 * 
 */
public interface IScalar<T> extends IValue
{
  /**
   * Pick up the Java value associated with this entity
   * 
   * @return an object.
   */
  T getValue();
}
