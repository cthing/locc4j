--Haskell Exercises
-------------------

fizzbuzz n | mod n 15 == 0 = "fizzbuzz"
           | mod n 5 == 0 = "fizz"
           | mod n 3 == 0 = "buzz"
           | otherwise = n


--Implicit type conversion
x = 2
y = x + 3.1

--Pattern matching
pts :: Int -> Int
pts 1 = 10
pts 2 = 6
pts 3 = 4
pts 4 = 3
pts 5 = 2
pts 6 = 1
pts _ = 0

--Actions
triangleArea = do
  putStrLn "The base?"
  b <- getLine
  putStrLn "The height?"
  h <- getLine
  putStrLn("The area of the triangle is " ++ show (read b * read h * 0.5))

name1 = do
  putStrLn "Name?"
  name <- getLine
  if name == "Simon" || name == "John" || name == "Phil"
    then putStrLn "Haskell is a great programming language."
    else if name == "Koen"
         then putStrLn "Debugging Haskell is fun."
         else putStrLn "I don't know you."

name2 = do
  putStrLn "Name?"
  name <- getLine
  putStrLn (message name)
    where greatLanguage = "Haskell is a great programming language."
          message "Simon" = greatLanguage
          message "John" = greatLanguage
          message "Phil" = greatLanguage
          message "Koen" = "Debugging haskell is fun."
          message _ = "I don't know you."

--Recursion
factorial :: Int -> Int
factorial 0 = 1
factorial n = n * factorial (n-1)
              --  fact 3
              --  |
              --  |   n is not 0, we calculate 3*fact 2
              --  |   |
              --  |   |   n is not 0, we calculate (3*fact 2)*fact 1
              --  |   |   |
              --  |   |   |   n is not 0, we calculate ((3*fact 2)*fact 1)*fact 0
              --  |   |   |   |
              --  |   |   |   |   factorial 0 returns 1
              --  |   |   |   |
              --  |   |   |   ((3*fact 2)*fact 1)*1
              --  |   |   |
              --  |   |   ((3*fact 2)*1)*1
              --  |   |
              --  |   ((3*2)*1)*1
              --  |
              --  6

doubleFactorial :: Int -> Int
doubleFactorial 0 = 1
doubleFactorial 1 = 1
doubleFactorial n = n * doubleFactorial (n-2)

mult :: Int -> Int -> Int
mult n 0 = 0
mult n 1 = n
mult n m = (mult n (m-1)) + n
           --  mult 5 4
           --  |
           --  |   m is not 0 or 1, we calculate (mult 5 3)+5
           --  |   |
           --  |   |   m is not 0 or 1, we calculate ((mult 5 2)+5)+5
           --  |   |   |
           --  |   |   |   m is not 0 or 1, we calculate (((mult 5 1)+5)+5)+5
           --  |   |   |   |
           --  |   |   |   |   mult 5 1 returns 5
           --  |   |   |   |
           --  |   |   |   (((5) + 5) + 5) + 5
           --  |   |   |
           --  |   |   ((10) + 5) + 5
           --  |   |
           --  |   (15) + 5
           --  |
           --  20

power :: Int -> Int -> Int
power x 0 = 1
power x 1 = x
power x y = (power x (y-1)) * x

fibonacci :: Int -> Int
fibonacci 0 = 0
fibonacci 1 = 1
fibonacci n
  | n >= 0 = fibonacci (n-1) + fibonacci (n-2)
  | otherwise = 0


--Will work only for Int since a float minus 1 might go under 0
--E.g.  addition :: (Eq a, Num a) => a -> a -> a   will not work
plusOne x = x + 1
addition :: Int -> Int -> Int
addition x 0 = x
addition x y = plusOne (addition x (y-1))

--Reimplementation of div (Integer division)
intDiv :: Int -> Int -> Int
intDiv x y
  | x > y = 1 + (div (x-y) y)
  | otherwise = 0

log2 :: Int -> Int
--log2 0 = do
log2 1 = 0
log2 2 = 1
log2 x = 1 + log2 (div x 2)

replicate2 :: Int -> a -> [a]
replicate2 0 x = []
replicate2 1 x = [x]
replicate2 n x = x : replicate (n - 1) x

(!!!) :: [a] -> Int -> a
(!!!) (x:xs) 0 = x
(!!!) (x:xs) n = (!!!) xs (n - 1)

(!!!!) :: [a] -> Int -> a
(!!!!) [a] 0 = head [a]
(!!!!) [a] n = (!!!) (tail [a]) (n - 1)

zip2 :: [a] -> [b] -> [(a, b)]
zip2 list [] = []
zip2 [] list = []
zip2 (x:xs) (y:ys) = (x,y) : zip2 xs ys
--zip2 _ _ = []




--Mapping exercises

--For the next five problems, try to make the type signature polymorphic
--takeInt :: Int -> [Int] -> [Int]
takeInt 0 (x:xs) = []
takeInt _ [] = []
takeInt i (x:xs) = x : takeInt (i - 1) xs

--dropInt :: Int -> [Int] -> [Int]
dropInt 0 (x:xs) = (x:xs)
dropInt _ [] = []
dropInt i (x:xs) = dropInt (i - 1) xs

--sumInt :: [Int] -> [Int]
sumInt [] = 0
sumInt (x:xs) = x + sumInt xs

--Pay attention to return types!!!
--Can't do x + takeInt 1 xs
--Int + [Int] is WRONG
scanSum :: [Int] -> [Int]
scanSum [] = []
scanSum (x:[]) = [x]
scanSum (x:xs) = x : scanSum ((x + sumInt (takeInt 1 xs)) : dropInt 1 xs)


--diffs :: [Int] -> [Int]
diffs [] = []
diffs (x:[]) = []
diffs (x:y:xs) = (y-x) : diffs (y:xs)


negateList1, negateList2 :: [Int] -> [Int]
negateList1 = map ((-) 0)
negateList2 l = map negate l --Using negate + explicitly writing argument

divisors :: Int -> [Int]
divisors p = [ f | f <- [1..p], p `mod` f == 0 ]
factors :: [Int] -> [[Int]]
factors = map divisors
factors2 :: [Int] -> [[Int]]
factors2 l = map divisors l
