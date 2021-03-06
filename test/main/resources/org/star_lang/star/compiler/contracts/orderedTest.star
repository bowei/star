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
orderedTest is package {

  type ordering is lt or gt or eq;

  contract ordered over %t where equality over %t is {
    compare has type (%t, %t) => ordering;
    minimum has type (%t, %t) => %t;
    minimum(x, y) default is
	  case compare(x, y) in {
	    lt is x;
	    eq is x;
	    gt is y;
	  };
    maximum has type (%t, %t) => %t;
    maximum(x, y) default is
 	  case compare(x, y) in {
	    lt is y;
	    eq is x;
	    gt is x;
	  };
  };
  
  implementation ordered over integer is {
    compare(X,X) is eq;
    compare(X,Y) where X>Y is gt;
    compare(_,_) default is lt;
  };

  foo has type (%t) => boolean where ordered over %t;
  foo(x) is x = x;

  main() do {
    assert minimum(3,4)=3;
  }
}