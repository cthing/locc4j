* Public class definition
CLASS cl_demo_rap_eml_u_calculator DEFINITION
  INHERITING FROM cl_demo_classrun
  PUBLIC
  CREATE PUBLIC.
  PUBLIC SECTION.
    METHODS main REDEFINITION.  " Comment 1
    METHODS constructor.

  PRIVATE SECTION.
    DATA:
      num1      TYPE c LENGTH 7,
      num2      TYPE c LENGTH 7,
      arithm_op TYPE c LENGTH 1.
    METHODS:
      initialize_dbtabs.
ENDCLASS.

* Public class implementation
CLASS cl_demo_rap_eml_u_calculator IMPLEMENTATION.
  METHOD main.

    " Comment 2
    cl_demo_input=>new(
     )->add_text( `RAP Calculator with a managed RAP BO`
     )->add_text( `Please enter numbers and an operand ` &&
                  `(+ - * P) for the calculation:`
     )->add_field( CHANGING field = num1
     )->add_line(
     )->add_field( CHANGING field = arithm_op
     )->add_line(
     )->add_field( CHANGING field = num2
     )->request( ).

    out->begin_section( `RAP Calculator with a managed RAP BO` ).

    TRY.
        MODIFY ENTITY demo_cs_rap_draft_m
         CREATE AUTO FILL CID
         FIELDS ( num1 num2 arithm_op ) WITH VALUE #(
          ( num1 =  num1
            num2 = num2
            arithm_op = arithm_op ) ).
      CATCH cx_root INTO FINAL(err).
    ENDTRY.

    IF err IS NOT INITIAL.
      out->write( err->get_text( ) ).
    ENDIF.

    COMMIT ENTITIES RESPONSE OF demo_cs_rap_draft_m REPORTED DATA(rep).

    IF sy-subrc <> 0.
      out->write_doc( `An issue occurred in the RAP save sequence.` ).
    ENDIF.

    IF rep-calc IS NOT INITIAL.
      out->write_html( '<b>The calculation cannot be executed:</b>' ).
      LOOP AT rep-calc
         ASSIGNING FIELD-SYMBOL(<reported>).
        out->write( <reported>-%msg->if_message~get_text( ) ).
      ENDLOOP.
    ENDIF.

    SELECT SINGLE num1, arithm_op, num2, calc_result
     FROM demo_cs_tab_calc INTO @FINAL(calculation).

    IF calculation IS NOT INITIAL.
      out->write_html(
             '<b>Calculation result</b>'
            )->write( calculation ).
    ENDIF.

  ENDMETHOD.
  METHOD initialize_dbtabs.
    DELETE FROM demo_cs_tab_calc.
  ENDMETHOD.
  METHOD constructor.
    super->constructor( ).
    initialize_dbtabs( ).
  ENDMETHOD.
ENDCLASS.
