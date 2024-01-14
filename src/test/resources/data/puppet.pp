class example::class(
  $param1,
  $param2=2,
  $param3=undef,  # pass this one
) {
  # comments are really simple
  some::resource {
    'bar':
      param1 => param2,
      # comments here too
      param3 => param4;
  }

  some::other::resource {
    'baz':
  }
}
