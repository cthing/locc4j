program class_t;
  class packet;
     // members in class
     integer size;
     integer payload [];
     integer i;

     // Constructor
     function new (integer size);
       begin
         this.size = size;
         payload = new[size];
         for (i=0; i < this.size; i ++) begin
           payload[i] = $random;
         end
       end
     endfunction

     // Task in class (object method)
     task print ();
       begin
         $write("Payload : ");
         for (i=0; i < size; i ++) begin
           $write("%x ",payload[i]);
         end
         $write("\n");
       end
     endtask

     /*
      * Function in class (object method)
      */
     function integer get_size();
       begin
         get_size = this.size;
       end
     endfunction
   endclass
   packet pkt;
   initial begin
     pkt = new(5);
     pkt.print();
     $display ("Size of packet %0d",pkt.get_size());
   end
endprogram
