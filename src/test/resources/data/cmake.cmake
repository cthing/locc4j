SET(_POSSIBLE_XYZ_INCLUDE include include/xyz)
SET(_POSSIBLE_XYZ_EXECUTABLE xyz)
SET(_POSSIBLE_XYZ_LIBRARY XYZ)

# this is a comment
IF(XYZ_FIND_VERSION_MAJOR AND XYZ_FIND_VERSION_MINOR)
  SET(_POSSIBLE_SUFFIXES "${XYZ_FIND_VERSION_MAJOR}${XYZ_FIND_VERSION_MINOR}" "${XYZ_FIND_VERSION_MAJOR}.${XYZ_FIND_VERSION_MINOR}" "-${XYZ_FIND_VERSION_MAJOR}.${XYZ_FIND_VERSION_MINOR}") # not counted
ELSE(XYZ_FIND_VERSION_MAJOR AND XYZ_FIND_VERSION_MINOR)
  SET(_POSSIBLE_SUFFIXES "67" "92" "352.9" "0.0.8z")
ENDIF(XYZ_FIND_VERSION_MAJOR AND XYZ_FIND_VERSION_MINOR)

FOREACH(_SUFFIX ${_POSSIBLE_SUFFIXES})
  LIST(APPEND _POSSIBLE_XYZ_INCLUDE "include/XYZ${_SUFFIX}")
  LIST(APPEND _POSSIBLE_XYZ_EXECUTABLE "XYZ${_SUFFIX}")
  LIST(APPEND _POSSIBLE_XYZ_LIBRARY "XYZ${_SUFFIX}")
ENDFOREACH(_SUFFIX) # not counted

FIND_PROGRAM(XYZ_EXECUTABLE
  NAMES ${_POSSIBLE_XYZ_EXECUTABLE}
)

# this is also a comment

