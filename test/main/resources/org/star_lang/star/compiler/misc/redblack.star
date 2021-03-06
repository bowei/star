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
redblack is package{
  -- inspired by Chris Okasaki's Purely FUnctional Data Structures

  type rbTree counts as redblack;

  private type color is red or black;

  private type rbTree of t where comparable over t is 
      rbEmpty or
      rbNode(color,rbTree of t,t,rbTree of t);

  private
  rbMember(_,rbEmpty) is false;
  rbMember(x,rbNode(_,L,y,R)) is
      x<y ? rbMember(x,L) |
      x>y ? rbMember(x,R) |
      false;

  private
  rbInsert(x,S) is let{
    ins(rbEmpty) is rbNode(black,rbEmpty,x,rbEmpty);
    ins(s matching rbNode(C,L,y,R)) is
	x<y ? balance(C,ins(L),y,R) |
	x>y ? balance(C,L,y,ins(R)) |
	s;
    var rbNode(_,A,Y,B) is ins(S);
  } in rbNode(black,A,Y,B);

  private
  balance(black,rbNode(red,rbNode(red,A,X,B),Y,C),Z,D) is
      rbNode(red,rbNode(black,A,X,B),Y,rbNode(black,C,Z,D));
  balance(black,rbNode(red,A,X,rbNode(red,B,Y,C)),Z,D) is
      rbNode(red,rbNode(black,A,X,B),Y,rbNode(black,C,Z,D));
  balance(black,A,X,rbNode(red,rbNode(red,B,Y,C),Z,D)) is
      rbNode(red,rbNode(black,A,X,B),Y,rbNode(black,C,Z,D));
  balance(black,A,X,rbNode(red,B,Y,rbNode(red,C,Z,D))) is
      rbNode(red,rbNode(black,A,X,B),Y,rbNode(black,C,Z,D));
  balance(C,A,Y,B) is
      rbNode(C,A,Y,B);

  private 
  foldRbLeft(F,St,rbEmpty) is St;
  foldRbLeft(F,St,rbNode(_,L,X,R)) is foldRbLeft(F,F(foldRbLeft(F,St,L),X),R);

  private
  foldRbRight(F,St,rbEmpty) is St;
  foldRbRight(F,St,rbNode(_,L,X,R)) is foldRbRight(F,F(X,foldRbRight(F,St,R)),L)


  -- Some tests
  main() do {
    T is rbInsert("beta",rbInsert("alpha",rbInsert("gamma",rbInsert("delta",rbEmpty))));
    logMsg(info,"T=$T");
  }
}