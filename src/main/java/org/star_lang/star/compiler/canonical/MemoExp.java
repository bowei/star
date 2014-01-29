package org.star_lang.star.compiler.canonical;

import org.star_lang.star.compiler.standard.StandardNames;
import org.star_lang.star.compiler.type.TypeUtils;
import org.star_lang.star.compiler.util.PrettyPrintDisplay;

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
public class MemoExp extends BaseExpression
{
  private final IContentExpression memo;
  private final IContentExpression[] freeVars;

  public MemoExp(Location loc, IContentExpression memo, IContentExpression[] freeVars)
  {
    super(loc, TypeUtils.functionType(memo.getType()));
    this.memo = memo;
    this.freeVars = freeVars;
  }

  @Override
  public void prettyPrint(PrettyPrintDisplay disp)
  {
    disp.appendWord(StandardNames.MEMO);
    memo.prettyPrint(disp);
  }

  public IContentExpression getMemo()
  {
    return memo;
  }

  public IContentExpression[] getFreeVars()
  {
    return freeVars;
  }

  @Override
  public void accept(CanonicalVisitor visitor)
  {
    visitor.visitMemo(this);
  }

  @Override
  public <A, E, P, C, D, T> E transform(TransformExpression<A, E, P, C, D, T> transform, T context)
  {
    return transform.transformMemo(this, context);
  }
}
