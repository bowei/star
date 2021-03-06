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
updateRels is package{
  -- test the updateable contract over cons lists

  R has type ref cons of ((string,integer));
  var R := cons of {
    ("a",1);
    ("b",2);
    ("a",2);
    ("a",3);
    ("b",1);
  };
    
  check has type (string,cons of ((string,integer))) =>boolean;
  check(S,Rr) is (S,_) in Rr;

  pairCheck(A,B) is (A,B) in R;
  
  main has type action();
  main() do {
	assert pairCheck("a", 1);
	assert pairCheck("a", 2);
	assert pairCheck("a", 3);
	assert pairCheck("b", 1);
	assert pairCheck("b", 2);
	assert not pairCheck("c", 1);
	assert not pairCheck("a", 4);
	
    extend R with ("b",3);
    assert pairCheck("b", 3);

    logMsg(info,"R before deleting a's is $R");
    delete ("a",_) in R;
    logMsg(info,"R after deleting \"a\" is $R");

	assert not pairCheck("a", 1);
	assert not pairCheck("a", 2);
	assert not pairCheck("a", 3);
	assert pairCheck("b", 1);
	assert pairCheck("b", 2);
	assert pairCheck("b", 3);
	assert not pairCheck("c", 1);
	assert not pairCheck("a", 4);
	
	update (N,X) in R with (N,X*2);
	logMsg(info,"R after updating \"b\" is $R");
	
	assert pairCheck("b",2);
	assert not pairCheck("b",1);
	assert pairCheck("b",4);
	assert pairCheck("b",6);
    assert not pairCheck("a", 4);
  }
}