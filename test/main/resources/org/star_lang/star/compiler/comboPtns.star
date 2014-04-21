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
comboPtns is package{
  
  comp has type ((%t) <= %t, (%t) <= %t) => ((%t) <= %t);
  comp(P1, P2) is (pattern (X) from P1(P2(X)));
   
  gt5(X) from X where X > 5;
  lt10(X) from X where X < 10;
   
  smPrimes is relation{2; 3; 5; 7; 11};

  medPrimes() is all X where (gt5(X) matching lt10(X)) in smPrimes;
   
  medPrimes2() is all X where comp(gt5, lt10)(X) in smPrimes;
   
  med is comp(gt5, lt10);
  medPrimes3() is all X where med(X) in smPrimes;
   
  main() do {
    assert medPrimes() = array of {7};
     
    assert medPrimes2() = array of {7};
     
    assert medPrimes3() = array of{7};
  }
}
