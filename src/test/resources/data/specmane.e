This is a comment
<'
// This is a dynamic class with two fields
struct packet_s {
  field0: uint (bits: 32);   // This field is called 'field0' and is a
                            // 32 bit wide unsigned integer.
  field1: byte;             // This field is called 'field1' and is a byte.

  -- This method is called once a packet_s object has been generated
  post_generate() is also {
    out(field0);            // Printing the value of 'field0'
  };
};

// This is a static class with a list of five packet struct
unit environment_u {
  my_pkt[5]: list of packet_s;
};

// sys is the root for every e environment and instantiates the 'test_env' object
extend sys {
  test_env: environment_u is instance;
};
'>
This is also a comment
