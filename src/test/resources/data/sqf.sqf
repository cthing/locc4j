// in-line comment that ends on new line

/* block comment that can span above multiple lines
and ends on the following character combination: */

// put the result of allUnits into a Variable
_unitsArray = allUnits;

// display the current Array size using systemChat
systemChat str count _unitsArray;

// create a new unit in the player group
group player createUnit ["B_RangeMaster_F", getPosATL player, [], 0, "FORM"];

// output the Array size again
systemChat str count _unitsArray;

// output the size of allUnits
systemChat str count allUnits;

// create some Array containing three arrays
_arr = [[1, 2, 3, 4, 5], [1, 2, 3, 4], [1, 2]];

// wrongly use the select operator to get the count of the third array
count _arr select 2; // error

// create a nested Array with 5 levels
_arr = [[[[[1]]]]];

// receive the nested number with some random math expressions
_arr select 0 select 1 - 1 select 15 / 3 - 5 select 0 select 10 * 10 + 4 * 0 - 100 // evaluates to 1
