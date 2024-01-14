namespace Ns
{
    /*

    multi-line comment

    */
    public class Cls
    {
        private const string BasePath = @"a:\";

        [Fact]
        public void MyTest()
        {
            // Arrange.
            Foo();

            // Act.
            Bar();

            // Assert.
            Baz();
        }
    }
}
