import star
buffer is package {
  type word8 is _word8(integer);
  word8 has type (integer) => word8;
  word8(o) where o >= 0 and o <= 255 is _word8(o);
  word8(o) is raise "word8 argument is not a word8";

  type word16 is _word16(integer);
  word16 has type (integer) => word16;
  word16(s) where s >= 0 and s <= 0xffff is _word16(s);
  word16(s) is raise "word16 argument is not a word16";

  type word32 is _word32(integer);
  word32(i) default is _word32(i);
  word32(nonInteger) is raise "nonInteger not a word32";

  type word64 is _word64(long);
  word64(l) default is _word64(l);
  word64(nonLong) is raise "nonLong not a word64";

  contract buffer over %b is {
    getWord8 has type (%b) => word8;
    putWord8 has type (%b, word8) => void;
    
    getWord16 has type (%b) => word16;
    putWord16 has type (%b, word16) => void;

    getWord32 has type (%b) => word32;
    putWord32 has type (%b, word32) => void;

    getWord64 has type (%b) => word64;
    putWord64 has type (%b, word64) => void;

    /* get/put buffer of a certain size */
    getBytes has type (%b, integer) => array of word8;      /* would have like word32, but underlying Java API has only int */
    putBytes has type (%b, array of word8) => void;         /* puts/appends contents of bytebuffer to first %b */
  };
}
