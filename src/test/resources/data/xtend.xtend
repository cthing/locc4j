// Testing

class Test {

    static def void main(String[] args) {
        /*
         * Multiline comment
         */
        val f = new Foo()
        f.bar() // Not counted
    }

}

class Foo {

    def bar() {
        println('string type 1')
        println("string type 2")
        println('''string type 3''')
    }

}
