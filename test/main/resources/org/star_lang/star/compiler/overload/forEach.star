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
forEach is package{
  forEach has type action(action(%a), %t) where sequence over %t determines %a;
  forEach(p, l) do {
	var r := l;
	while r matches _pair(x, xs) do {
		p(x);
		r := xs;
	}
  };
  
  main() do {
    forEach((procedure(X) do { logMsg(info,X); }), cons of {"alpha"; "beta"; "gamma"});
  }
}