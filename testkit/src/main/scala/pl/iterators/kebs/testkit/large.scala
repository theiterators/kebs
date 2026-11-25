package pl.iterators.kebs.testkit

// more cases than a method may have parameters, and than a closure may capture locals
object large {
  sealed trait Large
  final case class C1(n: Int, x: X1)     extends Large
  final case class C2(n: Int, x: X2)     extends Large
  final case class C3(n: Int, x: X3)     extends Large
  final case class C4(n: Int, x: X4)     extends Large
  final case class C5(n: Int, x: X5)     extends Large
  final case class C6(n: Int, x: X6)     extends Large
  final case class C7(n: Int, x: X7)     extends Large
  final case class C8(n: Int, x: X8)     extends Large
  final case class C9(n: Int, x: X9)     extends Large
  final case class C10(n: Int, x: X10)   extends Large
  final case class C11(n: Int, x: X11)   extends Large
  final case class C12(n: Int, x: X12)   extends Large
  final case class C13(n: Int, x: X13)   extends Large
  final case class C14(n: Int, x: X14)   extends Large
  final case class C15(n: Int, x: X15)   extends Large
  final case class C16(n: Int, x: X16)   extends Large
  final case class C17(n: Int, x: X17)   extends Large
  final case class C18(n: Int, x: X18)   extends Large
  final case class C19(n: Int, x: X19)   extends Large
  final case class C20(n: Int, x: X20)   extends Large
  final case class C21(n: Int, x: X21)   extends Large
  final case class C22(n: Int, x: X22)   extends Large
  final case class C23(n: Int, x: X23)   extends Large
  final case class C24(n: Int, x: X24)   extends Large
  final case class C25(n: Int, x: X25)   extends Large
  final case class C26(n: Int, x: X26)   extends Large
  final case class C27(n: Int, x: X27)   extends Large
  final case class C28(n: Int, x: X28)   extends Large
  final case class C29(n: Int, x: X29)   extends Large
  final case class C30(n: Int, x: X30)   extends Large
  final case class C31(n: Int, x: X31)   extends Large
  final case class C32(n: Int, x: X32)   extends Large
  final case class C33(n: Int, x: X33)   extends Large
  final case class C34(n: Int, x: X34)   extends Large
  final case class C35(n: Int, x: X35)   extends Large
  final case class C36(n: Int, x: X36)   extends Large
  final case class C37(n: Int, x: X37)   extends Large
  final case class C38(n: Int, x: X38)   extends Large
  final case class C39(n: Int, x: X39)   extends Large
  final case class C40(n: Int, x: X40)   extends Large
  final case class C41(n: Int, x: X41)   extends Large
  final case class C42(n: Int, x: X42)   extends Large
  final case class C43(n: Int, x: X43)   extends Large
  final case class C44(n: Int, x: X44)   extends Large
  final case class C45(n: Int, x: X45)   extends Large
  final case class C46(n: Int, x: X46)   extends Large
  final case class C47(n: Int, x: X47)   extends Large
  final case class C48(n: Int, x: X48)   extends Large
  final case class C49(n: Int, x: X49)   extends Large
  final case class C50(n: Int, x: X50)   extends Large
  final case class C51(n: Int, x: X51)   extends Large
  final case class C52(n: Int, x: X52)   extends Large
  final case class C53(n: Int, x: X53)   extends Large
  final case class C54(n: Int, x: X54)   extends Large
  final case class C55(n: Int, x: X55)   extends Large
  final case class C56(n: Int, x: X56)   extends Large
  final case class C57(n: Int, x: X57)   extends Large
  final case class C58(n: Int, x: X58)   extends Large
  final case class C59(n: Int, x: X59)   extends Large
  final case class C60(n: Int, x: X60)   extends Large
  final case class C61(n: Int, x: X61)   extends Large
  final case class C62(n: Int, x: X62)   extends Large
  final case class C63(n: Int, x: X63)   extends Large
  final case class C64(n: Int, x: X64)   extends Large
  final case class C65(n: Int, x: X65)   extends Large
  final case class C66(n: Int, x: X66)   extends Large
  final case class C67(n: Int, x: X67)   extends Large
  final case class C68(n: Int, x: X68)   extends Large
  final case class C69(n: Int, x: X69)   extends Large
  final case class C70(n: Int, x: X70)   extends Large
  final case class C71(n: Int, x: X71)   extends Large
  final case class C72(n: Int, x: X72)   extends Large
  final case class C73(n: Int, x: X73)   extends Large
  final case class C74(n: Int, x: X74)   extends Large
  final case class C75(n: Int, x: X75)   extends Large
  final case class C76(n: Int, x: X76)   extends Large
  final case class C77(n: Int, x: X77)   extends Large
  final case class C78(n: Int, x: X78)   extends Large
  final case class C79(n: Int, x: X79)   extends Large
  final case class C80(n: Int, x: X80)   extends Large
  final case class C81(n: Int, x: X81)   extends Large
  final case class C82(n: Int, x: X82)   extends Large
  final case class C83(n: Int, x: X83)   extends Large
  final case class C84(n: Int, x: X84)   extends Large
  final case class C85(n: Int, x: X85)   extends Large
  final case class C86(n: Int, x: X86)   extends Large
  final case class C87(n: Int, x: X87)   extends Large
  final case class C88(n: Int, x: X88)   extends Large
  final case class C89(n: Int, x: X89)   extends Large
  final case class C90(n: Int, x: X90)   extends Large
  final case class C91(n: Int, x: X91)   extends Large
  final case class C92(n: Int, x: X92)   extends Large
  final case class C93(n: Int, x: X93)   extends Large
  final case class C94(n: Int, x: X94)   extends Large
  final case class C95(n: Int, x: X95)   extends Large
  final case class C96(n: Int, x: X96)   extends Large
  final case class C97(n: Int, x: X97)   extends Large
  final case class C98(n: Int, x: X98)   extends Large
  final case class C99(n: Int, x: X99)   extends Large
  final case class C100(n: Int, x: X100) extends Large
  final case class C101(n: Int, x: X101) extends Large
  final case class C102(n: Int, x: X102) extends Large
  final case class C103(n: Int, x: X103) extends Large
  final case class C104(n: Int, x: X104) extends Large
  final case class C105(n: Int, x: X105) extends Large
  final case class C106(n: Int, x: X106) extends Large
  final case class C107(n: Int, x: X107) extends Large
  final case class C108(n: Int, x: X108) extends Large
  final case class C109(n: Int, x: X109) extends Large
  final case class C110(n: Int, x: X110) extends Large
  final case class C111(n: Int, x: X111) extends Large
  final case class C112(n: Int, x: X112) extends Large
  final case class C113(n: Int, x: X113) extends Large
  final case class C114(n: Int, x: X114) extends Large
  final case class C115(n: Int, x: X115) extends Large
  final case class C116(n: Int, x: X116) extends Large
  final case class C117(n: Int, x: X117) extends Large
  final case class C118(n: Int, x: X118) extends Large
  final case class C119(n: Int, x: X119) extends Large
  final case class C120(n: Int, x: X120) extends Large
  final case class C121(n: Int, x: X121) extends Large
  final case class C122(n: Int, x: X122) extends Large
  final case class C123(n: Int, x: X123) extends Large
  final case class C124(n: Int, x: X124) extends Large
  final case class C125(n: Int, x: X125) extends Large
  final case class C126(n: Int, x: X126) extends Large
  final case class C127(n: Int, x: X127) extends Large
  final case class C128(n: Int, x: X128) extends Large
  final case class C129(n: Int, x: X129) extends Large
  final case class C130(n: Int, x: X130) extends Large
  final case class C131(n: Int, x: X131) extends Large
  final case class C132(n: Int, x: X132) extends Large
  final case class C133(n: Int, x: X133) extends Large
  final case class C134(n: Int, x: X134) extends Large
  final case class C135(n: Int, x: X135) extends Large
  final case class C136(n: Int, x: X136) extends Large
  final case class C137(n: Int, x: X137) extends Large
  final case class C138(n: Int, x: X138) extends Large
  final case class C139(n: Int, x: X139) extends Large
  final case class C140(n: Int, x: X140) extends Large
  final case class C141(n: Int, x: X141) extends Large
  final case class C142(n: Int, x: X142) extends Large
  final case class C143(n: Int, x: X143) extends Large
  final case class C144(n: Int, x: X144) extends Large
  final case class C145(n: Int, x: X145) extends Large
  final case class C146(n: Int, x: X146) extends Large
  final case class C147(n: Int, x: X147) extends Large
  final case class C148(n: Int, x: X148) extends Large
  final case class C149(n: Int, x: X149) extends Large
  final case class C150(n: Int, x: X150) extends Large
  final case class X1(n: Int)
  final case class X2(n: Int)
  final case class X3(n: Int)
  final case class X4(n: Int)
  final case class X5(n: Int)
  final case class X6(n: Int)
  final case class X7(n: Int)
  final case class X8(n: Int)
  final case class X9(n: Int)
  final case class X10(n: Int)
  final case class X11(n: Int)
  final case class X12(n: Int)
  final case class X13(n: Int)
  final case class X14(n: Int)
  final case class X15(n: Int)
  final case class X16(n: Int)
  final case class X17(n: Int)
  final case class X18(n: Int)
  final case class X19(n: Int)
  final case class X20(n: Int)
  final case class X21(n: Int)
  final case class X22(n: Int)
  final case class X23(n: Int)
  final case class X24(n: Int)
  final case class X25(n: Int)
  final case class X26(n: Int)
  final case class X27(n: Int)
  final case class X28(n: Int)
  final case class X29(n: Int)
  final case class X30(n: Int)
  final case class X31(n: Int)
  final case class X32(n: Int)
  final case class X33(n: Int)
  final case class X34(n: Int)
  final case class X35(n: Int)
  final case class X36(n: Int)
  final case class X37(n: Int)
  final case class X38(n: Int)
  final case class X39(n: Int)
  final case class X40(n: Int)
  final case class X41(n: Int)
  final case class X42(n: Int)
  final case class X43(n: Int)
  final case class X44(n: Int)
  final case class X45(n: Int)
  final case class X46(n: Int)
  final case class X47(n: Int)
  final case class X48(n: Int)
  final case class X49(n: Int)
  final case class X50(n: Int)
  final case class X51(n: Int)
  final case class X52(n: Int)
  final case class X53(n: Int)
  final case class X54(n: Int)
  final case class X55(n: Int)
  final case class X56(n: Int)
  final case class X57(n: Int)
  final case class X58(n: Int)
  final case class X59(n: Int)
  final case class X60(n: Int)
  final case class X61(n: Int)
  final case class X62(n: Int)
  final case class X63(n: Int)
  final case class X64(n: Int)
  final case class X65(n: Int)
  final case class X66(n: Int)
  final case class X67(n: Int)
  final case class X68(n: Int)
  final case class X69(n: Int)
  final case class X70(n: Int)
  final case class X71(n: Int)
  final case class X72(n: Int)
  final case class X73(n: Int)
  final case class X74(n: Int)
  final case class X75(n: Int)
  final case class X76(n: Int)
  final case class X77(n: Int)
  final case class X78(n: Int)
  final case class X79(n: Int)
  final case class X80(n: Int)
  final case class X81(n: Int)
  final case class X82(n: Int)
  final case class X83(n: Int)
  final case class X84(n: Int)
  final case class X85(n: Int)
  final case class X86(n: Int)
  final case class X87(n: Int)
  final case class X88(n: Int)
  final case class X89(n: Int)
  final case class X90(n: Int)
  final case class X91(n: Int)
  final case class X92(n: Int)
  final case class X93(n: Int)
  final case class X94(n: Int)
  final case class X95(n: Int)
  final case class X96(n: Int)
  final case class X97(n: Int)
  final case class X98(n: Int)
  final case class X99(n: Int)
  final case class X100(n: Int)
  final case class X101(n: Int)
  final case class X102(n: Int)
  final case class X103(n: Int)
  final case class X104(n: Int)
  final case class X105(n: Int)
  final case class X106(n: Int)
  final case class X107(n: Int)
  final case class X108(n: Int)
  final case class X109(n: Int)
  final case class X110(n: Int)
  final case class X111(n: Int)
  final case class X112(n: Int)
  final case class X113(n: Int)
  final case class X114(n: Int)
  final case class X115(n: Int)
  final case class X116(n: Int)
  final case class X117(n: Int)
  final case class X118(n: Int)
  final case class X119(n: Int)
  final case class X120(n: Int)
  final case class X121(n: Int)
  final case class X122(n: Int)
  final case class X123(n: Int)
  final case class X124(n: Int)
  final case class X125(n: Int)
  final case class X126(n: Int)
  final case class X127(n: Int)
  final case class X128(n: Int)
  final case class X129(n: Int)
  final case class X130(n: Int)
  final case class X131(n: Int)
  final case class X132(n: Int)
  final case class X133(n: Int)
  final case class X134(n: Int)
  final case class X135(n: Int)
  final case class X136(n: Int)
  final case class X137(n: Int)
  final case class X138(n: Int)
  final case class X139(n: Int)
  final case class X140(n: Int)
  final case class X141(n: Int)
  final case class X142(n: Int)
  final case class X143(n: Int)
  final case class X144(n: Int)
  final case class X145(n: Int)
  final case class X146(n: Int)
  final case class X147(n: Int)
  final case class X148(n: Int)
  final case class X149(n: Int)
  final case class X150(n: Int)
  case object O1   extends Large
  case object O2   extends Large
  case object O3   extends Large
  case object O4   extends Large
  case object O5   extends Large
  case object O6   extends Large
  case object O7   extends Large
  case object O8   extends Large
  case object O9   extends Large
  case object O10  extends Large
  case object O11  extends Large
  case object O12  extends Large
  case object O13  extends Large
  case object O14  extends Large
  case object O15  extends Large
  case object O16  extends Large
  case object O17  extends Large
  case object O18  extends Large
  case object O19  extends Large
  case object O20  extends Large
  case object O21  extends Large
  case object O22  extends Large
  case object O23  extends Large
  case object O24  extends Large
  case object O25  extends Large
  case object O26  extends Large
  case object O27  extends Large
  case object O28  extends Large
  case object O29  extends Large
  case object O30  extends Large
  case object O31  extends Large
  case object O32  extends Large
  case object O33  extends Large
  case object O34  extends Large
  case object O35  extends Large
  case object O36  extends Large
  case object O37  extends Large
  case object O38  extends Large
  case object O39  extends Large
  case object O40  extends Large
  case object O41  extends Large
  case object O42  extends Large
  case object O43  extends Large
  case object O44  extends Large
  case object O45  extends Large
  case object O46  extends Large
  case object O47  extends Large
  case object O48  extends Large
  case object O49  extends Large
  case object O50  extends Large
  case object O51  extends Large
  case object O52  extends Large
  case object O53  extends Large
  case object O54  extends Large
  case object O55  extends Large
  case object O56  extends Large
  case object O57  extends Large
  case object O58  extends Large
  case object O59  extends Large
  case object O60  extends Large
  case object O61  extends Large
  case object O62  extends Large
  case object O63  extends Large
  case object O64  extends Large
  case object O65  extends Large
  case object O66  extends Large
  case object O67  extends Large
  case object O68  extends Large
  case object O69  extends Large
  case object O70  extends Large
  case object O71  extends Large
  case object O72  extends Large
  case object O73  extends Large
  case object O74  extends Large
  case object O75  extends Large
  case object O76  extends Large
  case object O77  extends Large
  case object O78  extends Large
  case object O79  extends Large
  case object O80  extends Large
  case object O81  extends Large
  case object O82  extends Large
  case object O83  extends Large
  case object O84  extends Large
  case object O85  extends Large
  case object O86  extends Large
  case object O87  extends Large
  case object O88  extends Large
  case object O89  extends Large
  case object O90  extends Large
  case object O91  extends Large
  case object O92  extends Large
  case object O93  extends Large
  case object O94  extends Large
  case object O95  extends Large
  case object O96  extends Large
  case object O97  extends Large
  case object O98  extends Large
  case object O99  extends Large
  case object O100 extends Large
  case object O101 extends Large
  case object O102 extends Large
  case object O103 extends Large
  case object O104 extends Large
  case object O105 extends Large
  case object O106 extends Large
  case object O107 extends Large
  case object O108 extends Large
  case object O109 extends Large
  case object O110 extends Large
  case object O111 extends Large
  case object O112 extends Large
  case object O113 extends Large
  case object O114 extends Large
  case object O115 extends Large
  case object O116 extends Large
  case object O117 extends Large
  case object O118 extends Large
  case object O119 extends Large
  case object O120 extends Large
}
