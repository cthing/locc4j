(* Merge two ordered lists using the order cmp.
 * Pre: each list must already be ordered per cmp.
 * Runs in O(n) time, where n = |xs| + |ys|.
 *)
fun merge cmp (xs, []) = xs
  | merge cmp (xs, y :: ys) = let
    fun loop (a, acc) (xs, []) = List.revAppend (a :: acc, xs)
      | loop (a, acc) (xs, y :: ys) =
        if cmp (a, y)
        then loop (y, a :: acc) (ys, xs)
        else loop (a, y :: acc) (xs, ys)
    in
        loop (y, []) (ys, xs)
    end
