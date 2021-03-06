package org.star_lang.star.compiler.cafe.compile.cont;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.star_lang.star.compiler.ErrorReport;
import org.star_lang.star.compiler.cafe.compile.CafeDictionary;
import org.star_lang.star.compiler.cafe.compile.CodeContext;
import org.star_lang.star.compiler.cafe.compile.ISpec;
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
public class SingleCont implements IContinuation
{
  private final AbstractInsnNode ins;
  private final boolean jump;
  private boolean used = false;

  public SingleCont(AbstractInsnNode ins, boolean jump)
  {
    this.ins = ins;
    this.jump = jump;
  }

  @Override
  public ISpec cont(ISpec src, CafeDictionary cxt, Location loc, ErrorReport errors, CodeContext ccxt)
  {
    assert !used : "this continuation has already been used";
    used = true;
    MethodNode mtd = ccxt.getMtd();
    mtd.instructions.add(ins);
    return src;
  }

  @Override
  public boolean isJump()
  {
    return jump;
  }
}
