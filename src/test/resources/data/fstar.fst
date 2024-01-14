
module Hello

(* multi
   line
   comment *)
open FStar.IO // uncounted comment

// single line comment
let main = print_string "Hello, F*!\n" (* uncounted comment *)
