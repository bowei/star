recordMap is package{
  type r is r{
    f has type ref dictionary of (string,integer)
  }
  
  var R is r{ f := dictionary of {"alpha"->1} }
  
  main() do {
    R.f["beta"] := 2
    
    assert R.f["alpha"] = 1
    assert R.f["beta"] = 2
  }
}
    