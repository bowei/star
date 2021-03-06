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
performTest is package{
  type maybe of %t is possible(%t) or impossible(exception);
  
  implementation (computation) over maybe is {
    _encapsulate(X) is possible(X);
    
    _combine(possible(X),F) is F(X);
    _combine(impossible(E),_) is impossible(E);
    
    _abort(R) is impossible(R);
    
    _handle(impossible(R),EF) is EF(R);
    _handle(M,EF) is M;
  }
  
  implementation execution over maybe is {    
    _perform(possible(X),_) is X;
    _perform(impossible(R),F) is F(R);
  }
  
  ff(LL) is maybe computation{
    for L in LL do{
      perform pp(L);
    }
    valis ();
  } 
  
  pp(L) is maybe computation{
    for (KK,V) in L do
      logMsg(info,"KK=$KK,V=$V");
      
    valis ()
  };
  
  doIf(LL,K) is maybe computation{
    if present LL[K] then
      valis ();
      
    raise "not found"
  }
  
  handle(LL,K) is maybe computation{
    try { 
      for (KK,V) in LL do {
        if V>K then
          raise "over #K"
      }
    } on abort { exception(_,XX,_) do logMsg(info,"abort message: #(XX cast string)") };
    valis ()
  }

  id(X) is X;
  
  main() do {
    L1 is list of {(1,"alpha"); (2,"beta"); (3,"gamma"); (4,"delta")};
    L2 is list of {(5,"eta")};
    MM is list of {L1; L2};
    
    perform ff(MM);
    
    perform doIf(L1,1);
    
    perform doIf(L1,1) on abort { X do logMsg(info,"Got exception (1): $X"); }
    
    perform doIf(L1,5) on abort { X do logMsg(info,"Got exception (2): $X"); }
    
    HH is handle(L1,"omega");
    
    perform HH;
    
    perform handle(L1,"beta");
  }
}