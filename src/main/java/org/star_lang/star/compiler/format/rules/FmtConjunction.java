package org.star_lang.star.compiler.format.rules;

import org.star_lang.star.compiler.ast.IAbstract;
import org.star_lang.star.compiler.format.FormatRanges;
import org.star_lang.star.compiler.standard.StandardNames;
import org.star_lang.star.compiler.util.PrettyPrintDisplay;
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
public class FmtConjunction implements FmtFormatOp
{
  private final FmtFormatOp lhs;
  private final FmtFormatOp rhs;

  public FmtConjunction(Location loc, FmtFormatOp lhs, FmtFormatOp rhs)
  {
    this.lhs = lhs;
    this.rhs = rhs;
  }

  @Override
  public void format(IAbstract term, Location loc, IAbstract[] env, FormatRanges ranges)
  {
    lhs.format(term, loc, env, ranges);
    rhs.format(term, loc, env, ranges);
  }

  @Override
  public void prettyPrint(PrettyPrintDisplay disp)
  {
    lhs.prettyPrint(disp);
    disp.append(StandardNames.WFF_AND);
    rhs.prettyPrint(disp);
  }
}
