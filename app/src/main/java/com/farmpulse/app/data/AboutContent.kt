package com.farmpulse.app.data

/** เนื้อหาสำหรับหน้า "เกี่ยวกับโครงการ" — แบ่งเป็นส่วนย่อยให้อ่านง่าย ไม่ใช่ข้อความยาวก้อนเดียว */
object AboutContent {

    /** ตัวเลขสำคัญที่ดึงออกมาเน้นเป็นการ์ด ให้เห็นภาพปัญหาได้ใน 3 วินาที */
    data class Stat(val number: String, val caption: String)

    val stats = listOf(
        Stat("12 ล้าน", "แรงงานภาคเกษตรไทย\n(30.11% ของแรงงานทั้งหมด)"),
        Stat("26%", "ครัวเรือนเกษตร\nที่เข้าถึงระบบชลประทาน"),
        Stat("30%", "ปริมาณน้ำที่ลดได้\nด้วยระบบรดน้ำอัจฉริยะ")
    )

    const val problemHeading = "ปัญหาที่เราเห็น"
    const val problemText =
        "ภาคเกษตรกรรมเป็นรากฐานสำคัญของเศรษฐกิจไทย แต่เกษตรกรส่วนใหญ่ยังต้องดูแลพืชผลด้วยแรงงานคนเป็นหลัก " +
        "โดยเฉพาะการรดน้ำที่ต้องอาศัยความสม่ำเสมอและความแม่นยำ\n\n" +
        "ขณะเดียวกัน มีครัวเรือนเกษตรเพียงส่วนน้อยที่เข้าถึงระบบชลประทานได้ " +
        "สะท้อนความเปราะบางในการจัดการน้ำ โดยเฉพาะช่วงที่สภาพอากาศแปรปรวน " +
        "ซึ่งกระทบโดยตรงต่อผลผลิตและรายได้"

    const val gapHeading = "ช่องว่างที่ยังไม่มีใครแก้"
    const val gapText =
        "เทคโนโลยี IoT ช่วยลดการใช้น้ำได้อย่างมีนัยสำคัญ แต่ระบบในท้องตลาดส่วนใหญ่ " +
        "มีต้นทุนสูง ใช้งานซับซ้อน และต้องอาศัยความรู้ทางเทคนิคระดับสูง\n\n" +
        "อุปสรรคเหล่านี้ทำให้เกิดความเหลื่อมล้ำทางดิจิทัล — เกษตรกรรายย่อยที่ควรได้ประโยชน์มากที่สุด " +
        "กลับเป็นกลุ่มที่เข้าถึงได้ยากที่สุด"

    const val solutionHeading = "สิ่งที่เราทำ"
    const val solutionText =
        "FarmRak คือระบบรดน้ำและให้ปุ๋ยอัตโนมัติที่ออกแบบมาเพื่อเกษตรกรไทยโดยเฉพาะ:\n\n" +
        "• ประกอบเองได้ ต้นทุนต่ำ ใช้อุปกรณ์หาซื้อง่าย\n" +
        "• ทำงานแบบ Local ไม่ต้องพึ่งอินเทอร์เน็ต\n" +
        "• ควบคุมผ่านมือถือด้วยหน้าจอภาษาไทยที่อ่านง่าย\n" +
        "• ดูแลรักษาและต่อยอดเองได้ในชุมชน\n\n" +
        "เป้าหมายของเราไม่ใช่แค่สร้างเครื่องมือ แต่คือการส่งต่อองค์ความรู้ให้ชุมชนพึ่งพาตัวเองได้อย่างยั่งยืน"

    val historyReferences = listOf(
        PlantReference(
            "Gupta, S. et al. (2025). Smart agriculture using IoT for automated irrigation, water and energy efficiency. Smart Agricultural Technology, 12, 101081.",
            "https://doi.org/10.1016/j.atech.2025.101081"
        ),
        PlantReference(
            "United Nations Thailand. (2020). Thai agricultural sector: From problems to solutions.",
            "https://thailand.un.org/en/103307-thai-agricultural-sector-problems-solutions"
        ),
        PlantReference(
            "World Bank. (2023). Employment in agriculture (% of total employment) – Thailand.",
            "https://data.worldbank.org/indicator/SL.AGR.EMPL.ZS?locations=TH"
        )
    )

    const val programText = "โครงการภายใต้ Community Engagement Program ปีที่ 2\nโรงเรียนมหิดลวิทยานุสรณ์"

    val teamMembers = listOf(
        "นางสาวสุพิชญา บัวคีรี (หัวหน้าโครงการ)",
        "นายภากร วงษ์อรุณ (ผู้พัฒนาแอป)",
        "นายวรวิทย์ แดนขนาน",
        "นางสาวฟ้างาม กำมะหยี่",
        "นางสาวณิชนันทน์ สุขม่วง",
        "นายตะลันต์ ลออวงศ์",
        "นายน้ำเหนือ อินต๊ะใจ"
    )

    val advisors = listOf(
        "อาจารย์นงลักษณ์ อาภาสัตย์",
        "ดร.ณรงค์ศักดิ์ ขุนรักษา"
    )
}
