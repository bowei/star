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
nestedLetTuple is package{
  foo() is let{
    (a, (b, c)) is (1, (2, 3));
  } in (a,b,c);
  
  bar(X) is let{
    (a, (b, c)) is X;
  } in (a,b,c);
  
  foo2 is let {
    var ((a,b), c) is ((1,2),3);
  } in 0;
  
  main() do {
    assert foo()=(1,2,3);
    
    assert bar((1,(2,3))) = (1,2,3);
    
    assert foo2=0;
  }
}
    
  