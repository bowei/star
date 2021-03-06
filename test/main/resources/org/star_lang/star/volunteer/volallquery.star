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

volallquery is connections {
  originate(Port_0,{DELETE has type(string) => boolean;
                          Folder has type ref list of ((string, boolean))});
  respond(Port_1,{DELETE has type(string) => boolean;
                  Folder has type ref list of ((string, boolean));
                  report has type action()});
  -- connect(Port_0, Port_1,(volunteer DELETE(x0) as DELETE(x0)));
  connect(Port_0, Port_1,(volunteer query X as X));
}