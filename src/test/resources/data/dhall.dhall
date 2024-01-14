{- A comment within the interior of a multi-line literal counts as part of the
   literal
-}

''
-- Hello
{- world -}
''
{ some = "thing"

, keys = ["can"
, "have",
-- wait for it
"lists"]
}
