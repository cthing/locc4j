pragma solidity >=0.4.22 <0.6.0;

// Comment line
contract Foo {
    /*
     Comment line
     Comment line
     Comment line
     */
    function foo(address bar) public {
         require(bar != 0);
    }
}
