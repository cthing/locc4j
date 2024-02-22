--
-- This is the city wanderer.  You can walk N, S, E, or W some number
-- of blocks, and it will tell you how to get home.
--
package Wander is
   -- Directions to wander.  This is a public type; all its values are
   -- visible to the client programs.
   type Direction_Instruction is (North, South, East, West, Stop);
   subtype Direction is Direction_Instruction range North..West;

   -- Here are the "objects" being described.
   type Locator is private;

   -- Walk in a particular direction a particular distance.
   procedure Walk(L: in out Locator; D: Direction; NBlock: Integer);

   -- How far is it from home (total walking distance)?
   function How_Far_Home(L: Locator) return Integer;

   -- If I want to walk home from where I am now, which way should I start,
   -- and how far should I go in that direction?  If I am home, the
   -- direction will be Stop, and the distance will be zero.
   function Toward_Home(L: Locator) return Direction_Instruction;
   function Toward_Home(L: Locator) return Integer;

private
   -- Your location is just a two-dimensional position.  Note that the
   -- initial values for the fields are essentially the constructor for
   -- the objects.
   type Locator is record
      NS: Integer := 0;      -- North-south position (North is positive)
      EW: Integer := 0;      -- East-West position (East is positive)
   end record;
end Wander;
