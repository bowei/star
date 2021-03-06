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
import volunteers;
import ports;
 
volrequest3 is connections {
  originate(p1,{DO has type action(string, integer); ZERO has type action(); ONE has type action(string);THREE has type action(string,integer,string)});
  respond(p2,{OD has type action(string, integer); OREZ has type action(); ENO has type action(string); EERHT has type action(string,string,integer)});
  connect(p1,p2,(volunteer DO(A, B) as OD(A, B)));
  connect(p1,p2,(volunteer ZERO() as OREZ()));
  connect(p1,p2,(volunteer ONE(X) as ENO(X)));
  connect(p1,p2,(volunteer THREE(A, B, C) as EERHT(A, C, B)));
}