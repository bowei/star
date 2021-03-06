Routes is package {
  type OperationName is alias of string;

  operationNameEqual(n1, n2) is (n1 = n2);

  type Operation of %op is Operation(OperationName, %op);

  operationEquals(Operation(n1, o1), Operation(n2, o2)) is
    operationNameEqual(n1, n2) and (o1 = o2)

/*
 * A route specifies a sequence of production steps associated with a product.
 */

  type Route of %op is Route{
    rem has type RouteRem of %op;
  };

  routeEquals has type (Route of %op, Route of %op) => boolean where equality over %op;
  routeEquals(r1, r2) is r1.rem = r2.rem;

  implementation equality over (Route of %op where equality over %op) is {
    (=) = routeEquals;
  };

  /* remainder of route */
  type RouteRem of %op is
     RouteList(RouteElement of %op)
     /*
      * First route is the qt zone, second one is what comes after,
      * so the outermost one is one that ends next.
      */
  or RouteQTLimit(RouteRem of %op, RouteRem of %op);

  routeRemEquals has type (RouteRem of %op, RouteRem of %op) => boolean where equality over %op;
  routeRemEquals(RouteList(l1), RouteList(l2)) is l1 = l2;
  routeRemEquals(RouteQTLimit(r1in, r1after), RouteQTLimit(r2in, r2after)) is
    routeRemEquals(r2in, r2in) and routeRemEquals(r2after, r2after)
  routeRemEquals(_, _) default is false;

  implementation equality over (RouteRem of %op where equality over %op) is {
    (=) = routeRemEquals;
  };
  
  type RouteElement of %op is
      RouteOp(Operation of %op)
    /* #### nesting does not necessarily nest durations; should enforce */
    or RouteQTZone(RouteRem of %op); /* route must not be empty */

  routeElementEquals has type ((RouteElement of %op , RouteElement of %op) => boolean) where equality over %op;
  routeElementEquals(RouteOp(op1), RouteOp(op2)) is operationEquals(op1, op2);
  routeElementEquals(RouteQTZone(r1), RouteQTZone(r2)) is
    routeRemEquals(r1, r2);
  routeElementEquals(_, _) default is false;

  implementation equality over (RouteElement of %op where equality over %op) is {
    (=) = routeElementEquals;
  };

}
