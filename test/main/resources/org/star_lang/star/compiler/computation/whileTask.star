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
whileTask is package{
  import tasking;
  
  tt(X) is task{
    valis X+2;
  }
  
  ww(X) is task{
    var C:=0;
    
    while C<10 do{
      XX is valof tt(X);
      C := XX+C;
    }
    valis C+1;
  }
  
  main() do{
   ZZ is valof ww(1);
     
   assert ZZ = 13;
  }
}