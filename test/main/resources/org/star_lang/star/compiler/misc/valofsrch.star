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
valofsrch is package{

  type change is changed or noChange;

  setAttributes(names, attrs) is valof {
  --    logMsg(info,"Got begin update list $names");
      if "ignre" in names then { valis changed }
      else { valis noChange;}
  }
  
  main() do {
    R is setAttributes(list of {"ignre"; "not"}, {ignre="nothing"; nt=1});
    assert R = changed;
  }
}
