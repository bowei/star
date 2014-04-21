package org.star_lang.star.compiler.canonical;

import com.starview.platform.data.type.IType;

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
public interface IContentPattern extends Canonical
{
  /**
   * The type of the pattern. Technically, the type of values that this pattern matches.
   * 
   * @return
   */
  IType getType();

  /**
   * Allow a transformer to transform this pattern
   * 
   * @param transform
   * @return the transformed entity. May not be a pattern, depends on the transform
   */
  <A, E, P, C, D, T> P transformPattern(TransformPattern<A, E, P, C, D, T> transform, T context);
}