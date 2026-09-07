package com.farmpulse.app.data

/**
 * ข้อมูลพืชอ้างอิงสำหรับ "ตำราพืช" — เน้นพืชสวนครัวไทยที่ปลูกกันทั่วไป
 * ค่า pH/ความชื้น/ปุ๋ยเป็นช่วงค่าทั่วไปสำหรับการปลูกแบบเกษตรอินทรีย์ อาจแตกต่างไปตามสายพันธุ์และสภาพดินจริง
 * ควรใช้ประกอบการตัดสินใจร่วมกับประสบการณ์หน้างานและแหล่งข้อมูลเพิ่มเติมท้ายแต่ละพืช
 */
object PlantData {

    private val generalReferences = listOf(
        PlantReference("กรมวิชาการเกษตร", "https://www.doa.go.th"),
        PlantReference("กรมส่งเสริมการเกษตร", "https://www.doae.go.th"),
        PlantReference("มหาวิทยาลัยเกษตรศาสตร์", "https://www.ku.ac.th")
    )

    private val allUnsorted: List<Plant> = listOf(
        Plant(
            id = "holy_basil",
            thaiName = "กะเพรา",
            scientificName = "Ocimum tenuiflorum",
            category = PlantCategory.HERB,
            photoRes = com.farmpulse.app.R.drawable.photo_holy_basil,
            description = "สมุนไพรกลิ่นฉุนที่ใช้ในอาหารไทยแทบทุกครัวเรือน ปลูกง่าย โตเร็ว ชอบแดดจัด ทนแล้งได้ดีกว่าโหระพา เก็บใบได้ต่อเนื่องถ้าตัดแต่งกิ่งสม่ำเสมอ",
            phMin = 6.0f, phMax = 7.5f,
            moistureMin = 40, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอกหรือปุ๋ยหมักที่ย่อยสลายดีแล้ว ใส่ทุก 2-3 สัปดาห์ หลีกเลี่ยงปุ๋ยไนโตรเจนสูงเกินไปเพราะจะทำให้ใบบางกลิ่นจาง",
            references = generalReferences
        ),
        Plant(
            id = "thai_basil",
            thaiName = "โหระพา",
            scientificName = "Ocimum basilicum",
            category = PlantCategory.HERB,
            photoRes = com.farmpulse.app.R.drawable.photo_thai_basil,
            description = "กลิ่นหอมอ่อนกว่ากะเพรา ใช้แต่งกลิ่นแกงและผัด ชอบดินชื้นสม่ำเสมอมากกว่ากะเพรา ไม่ทนแล้งเท่า ควรปลูกในที่มีร่มเงาบางส่วนช่วงแดดจัด",
            phMin = 6.0f, phMax = 7.0f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยหมักหรือปุ๋ยมูลไส้เดือน ใส่เดือนละครั้ง เน้นบำรุงใบให้เขียวหนา",
            references = generalReferences
        ),
        Plant(
            id = "thai_chili",
            thaiName = "พริกขี้หนู",
            scientificName = "Capsicum annuum",
            category = PlantCategory.FRUIT_VEG,
            photoRes = com.farmpulse.app.R.drawable.photo_thai_chili,
            description = "พืชยืนต้นอายุสั้นที่ให้ผลผลิตต่อเนื่องหลายเดือน ต้องการแดดเต็มวัน ระบายน้ำดี รากเน่าง่ายถ้าดินแฉะเกินไป",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 45, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอกเก่ารองพื้นก่อนปลูก แล้วเสริมปุ๋ยหมักช่วงออกดอกติดผลเพื่อให้ติดผลดก",
            references = generalReferences
        ),
        Plant(
            id = "thai_eggplant",
            thaiName = "มะเขือเปราะ",
            scientificName = "Solanum virginianum",
            category = PlantCategory.FRUIT_VEG,
            photoRes = com.farmpulse.app.R.drawable.photo_thai_eggplant,
            description = "ผักพื้นบ้านที่ให้ผลผลิตยาวนาน ชอบดินร่วนอุดมสมบูรณ์ ต้องการน้ำสม่ำเสมอโดยเฉพาะช่วงติดผล",
            phMin = 5.5f, phMax = 6.8f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักรองพื้น ร่วมกับปุ๋ยมูลค้างคาวหรือมูลไก่ตากแห้งช่วงติดผลเพื่อเร่งผลผลิต",
            references = generalReferences
        ),
        Plant(
            id = "tomato",
            thaiName = "มะเขือเทศ",
            scientificName = "Solanum lycopersicum",
            category = PlantCategory.FRUIT_VEG,
            photoRes = com.farmpulse.app.R.drawable.photo_tomato,
            description = "ต้องการแสงแดดเต็มที่และค้างยึดลำต้น ดินต้องระบายน้ำดีมากเพราะรากเน่าง่าย ผลแตกง่ายถ้าน้ำไม่สม่ำเสมอ",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 55, moistureMax = 70,
            fertilizerAdvice = "ปุ๋ยหมักผสมมูลไก่ตากแห้ง เน้นให้สม่ำเสมอ ลดน้ำหนักการรดน้ำแปรปรวนเพื่อป้องกันผลแตก",
            references = generalReferences
        ),
        Plant(
            id = "cucumber",
            thaiName = "แตงกวา",
            scientificName = "Cucumis sativus",
            category = PlantCategory.VINE,
            photoRes = com.farmpulse.app.R.drawable.photo_cucumber,
            description = "ไม้เลื้อยที่ต้องการค้างหรือซุ้ม ให้ผลเร็วภายใน 30-40 วัน ต้องการน้ำมากและสม่ำเสมอ ถ้าขาดน้ำผลจะขม",
            phMin = 5.5f, phMax = 6.8f,
            moistureMin = 60, moistureMax = 75,
            fertilizerAdvice = "ปุ๋ยคอกเก่าหรือปุ๋ยหมักรองพื้น เสริมปุ๋ยน้ำหมักชีวภาพช่วงออกดอก",
            references = generalReferences
        ),
        Plant(
            id = "water_spinach",
            thaiName = "ผักบุ้ง",
            scientificName = "Ipomoea aquatica",
            category = PlantCategory.LEAFY,
            photoRes = com.farmpulse.app.R.drawable.photo_water_spinach,
            description = "ผักที่ปลูกง่ายที่สุดชนิดหนึ่ง ชอบดินชื้นแฉะถึงน้ำขัง ปลูกในกระถางไม่มีรูระบายก็ได้ เก็บเกี่ยวได้เร็วภายใน 25-30 วัน",
            phMin = 5.5f, phMax = 7.0f,
            moistureMin = 70, moistureMax = 90,
            fertilizerAdvice = "ปุ๋ยคอกหรือปุ๋ยหมักทั่วไป ใส่หลังตัดเก็บเกี่ยวแต่ละรอบเพื่อให้แตกยอดใหม่เร็ว",
            references = generalReferences
        ),
        Plant(
            id = "chinese_kale",
            thaiName = "คะน้า",
            scientificName = "Brassica oleracea",
            category = PlantCategory.LEAFY,
            photoRes = com.farmpulse.app.R.drawable.photo_chinese_kale,
            description = "ผักตระกูลกะหล่ำที่นิยมปลูกมาก ต้องการธาตุอาหารสูงเพราะโตเร็ว ชอบอากาศเย็นกว่าผักบุ้ง ทนแดดจัดได้น้อยกว่า",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 60, moistureMax = 75,
            fertilizerAdvice = "ปุ๋ยหมักหรือมูลไก่ตากแห้ง ใส่ทุก 2 สัปดาห์เพราะเป็นผักที่กินธาตุอาหารเยอะ",
            references = generalReferences
        ),
        Plant(
            id = "lemongrass",
            thaiName = "ตะไคร้",
            scientificName = "Cymbopogon citratus",
            category = PlantCategory.HERB,
            photoRes = com.farmpulse.app.R.drawable.photo_lemongrass,
            description = "พืชตระกูลหญ้าที่ทนทานมาก ปลูกครั้งเดียวเก็บเกี่ยวได้หลายปี ทนแล้งได้ดี เหมาะปลูกริมรั้วหรือแนวกันดินพัง",
            phMin = 5.5f, phMax = 7.5f,
            moistureMin = 40, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอกทุก 1-2 เดือน ไม่ต้องการธาตุอาหารมาก",
            references = generalReferences
        ),
        Plant(
            id = "galangal",
            thaiName = "ข่า",
            scientificName = "Alpinia galanga",
            category = PlantCategory.ROOT,
            photoRes = com.farmpulse.app.R.drawable.photo_galangal,
            description = "เหง้าใต้ดินที่ใช้แต่งกลิ่นต้มยำ ชอบดินร่วนซุยระบายน้ำดี ไม่ชอบน้ำขังที่โคน ปลูกในร่มเงาบางส่วนได้ดี",
            phMin = 5.5f, phMax = 6.5f,
            moistureMin = 55, moistureMax = 70,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักช่วงแตกหน่อใหม่ ควรพูนดินโคนต้นเป็นระยะ",
            references = generalReferences
        ),
        Plant(
            id = "ginger",
            thaiName = "ขิง",
            scientificName = "Zingiber officinale",
            category = PlantCategory.ROOT,
            photoRes = com.farmpulse.app.R.drawable.photo_ginger,
            description = "เหง้าใต้ดินที่ต้องการดินร่วนซุยอุดมสมบูรณ์ ชอบร่มเงาบางส่วน ไม่ทนแดดจัดทั้งวัน ต้องการความชื้นสม่ำเสมอ",
            phMin = 5.5f, phMax = 6.5f,
            moistureMin = 60, moistureMax = 75,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักรองพื้นก่อนปลูกให้หนา เพราะขิงกินธาตุอาหารตลอดช่วงเจริญเติบโต",
            references = generalReferences
        ),
        Plant(
            id = "yardlong_bean",
            thaiName = "ถั่วฝักยาว",
            scientificName = "Vigna unguiculata",
            category = PlantCategory.VINE,
            photoRes = com.farmpulse.app.R.drawable.photo_yardlong_bean,
            description = "ไม้เลื้อยตระกูลถั่วที่ตรึงไนโตรเจนในดินได้เอง จึงไม่ต้องการปุ๋ยไนโตรเจนมาก ต้องการค้างให้เลื้อยและแดดเต็มวัน",
            phMin = 5.5f, phMax = 6.8f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอกเก่าปริมาณน้อยรองพื้นพอ เน้นฟอสฟอรัส-โพแทสเซียมมากกว่าไนโตรเจนเพื่อให้ติดฝักดี",
            references = generalReferences
        ),
        Plant(
            id = "shallot",
            thaiName = "หอมแดง",
            scientificName = "Allium ascalonicum",
            category = PlantCategory.ROOT,
            photoRes = com.farmpulse.app.R.drawable.photo_shallot,
            description = "พืชหัวที่ต้องการดินร่วนระบายน้ำดีมาก แฉะไม่ได้เด็ดขาดเพราะหัวจะเน่า ชอบแดดจัดและอากาศแห้ง",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 45, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอกเก่าผสมขี้เถ้าแกลบรองพื้น ช่วยให้ดินโปร่งระบายน้ำดีขึ้น",
            references = generalReferences
        ),
        Plant(
            id = "garlic",
            thaiName = "กระเทียม",
            scientificName = "Allium sativum",
            category = PlantCategory.ROOT,
            photoRes = com.farmpulse.app.R.drawable.photo_garlic,
            description = "ปลูกได้ดีในฤดูหนาวที่อากาศแห้งเย็น ต้องการดินร่วนระบายน้ำดี ไม่ชอบดินเหนียวแฉะ",
            phMin = 6.0f, phMax = 7.0f,
            moistureMin = 45, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอกเก่ารองพื้นก่อนปลูกเท่านั้น ระหว่างเติบโตไม่ต้องเติมมาก",
            references = generalReferences
        ),
        Plant(
            id = "lime",
            thaiName = "มะนาว",
            scientificName = "Citrus aurantiifolia",
            category = PlantCategory.TREE,
            photoRes = com.farmpulse.app.R.drawable.photo_lime,
            description = "ไม้ยืนต้นที่ให้ผลผลิตยาวนานหลายปี ต้องการแดดเต็มวันและดินระบายน้ำดี อ่อนแอต่อโรครากเน่าถ้าน้ำขัง",
            phMin = 6.0f, phMax = 6.5f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักรอบทรงพุ่มปีละ 2-3 ครั้ง ใส่ช่วงต้นฝนและหลังเก็บผลผลิต",
            references = generalReferences
        ),
        Plant(
            id = "napa_cabbage",
            thaiName = "ผักกาดขาว",
            scientificName = "Brassica rapa subsp. pekinensis",
            category = PlantCategory.LEAFY,
            photoRes = com.farmpulse.app.R.drawable.photo_napa_cabbage,
            description = "ผักตระกูลกะหล่ำ ใบห่อเป็นปลี เนื้อนุ่ม นิยมใช้ผัดหรือต้มจืด ชอบอากาศเย็นกว่าผักใบทั่วไป",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 60, moistureMax = 75,
            fertilizerAdvice = "ปุ๋ยหมัก/มูลไก่ตากแห้ง เน้นช่วงแรกให้ใบโตไว แล้วลดลงตอนเริ่มห่อปลี",
            references = generalReferences
        ),
        Plant(
            id = "choy_sum",
            thaiName = "กวางตุ้ง",
            scientificName = "Brassica rapa var. parachinensis",
            category = PlantCategory.LEAFY,
            photoRes = com.farmpulse.app.R.drawable.photo_choy_sum,
            description = "ผักใบเขียวก้านอวบ โตเร็ว เก็บเกี่ยวได้ภายใน 30-35 วัน นิยมผัดน้ำมันหอย",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 60, moistureMax = 75,
            fertilizerAdvice = "ปุ๋ยหมักหรือมูลไก่ ใส่ทุก 2 สัปดาห์เพราะโตเร็วกินธาตุอาหารมาก",
            references = generalReferences
        ),
        Plant(
            id = "amaranth",
            thaiName = "ผักโขม",
            scientificName = "Amaranthus tricolor",
            category = PlantCategory.LEAFY,
            photoRes = com.farmpulse.app.R.drawable.photo_amaranth,
            description = "ใบอ่อนกินได้ โตเร็วมาก ทนร้อนได้ดีกว่าผักใบชนิดอื่น เหมาะปลูกช่วงหน้าร้อน",
            phMin = 6.0f, phMax = 7.0f,
            moistureMin = 55, moistureMax = 70,
            fertilizerAdvice = "ปุ๋ยคอกหรือปุ๋ยหมักทั่วไป ไม่ต้องดูแลมากเพราะทนทานอยู่แล้ว",
            references = generalReferences
        ),
        Plant(
            id = "ivy_gourd",
            thaiName = "ตำลึง",
            scientificName = "Coccinia grandis",
            category = PlantCategory.VINE,
            photoRes = com.farmpulse.app.R.drawable.photo_ivy_gourd,
            description = "ไม้เลื้อยยืนต้น ปลูกครั้งเดียวเก็บยอดกินได้นานหลายปี ทนแล้งดี ขึ้นง่ายตามรั้วบ้าน",
            phMin = 5.5f, phMax = 7.0f,
            moistureMin = 45, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอกทุก 1-2 เดือนก็เพียงพอ ไม่ต้องการธาตุอาหารมาก",
            references = generalReferences
        ),
        Plant(
            id = "angled_luffa",
            thaiName = "บวบ",
            scientificName = "Luffa acutangula",
            category = PlantCategory.VINE,
            photoRes = com.farmpulse.app.R.drawable.photo_angled_luffa,
            description = "ไม้เลื้อยต้องการค้างให้เกาะ ผลอ่อนใช้แกงหรือผัด เก็บเกี่ยวได้ภายใน 50-60 วัน",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 60, moistureMax = 75,
            fertilizerAdvice = "ปุ๋ยหมักผสมมูลไก่ตากแห้งช่วงออกดอกติดผลเพื่อเร่งผลผลิต",
            references = generalReferences
        ),
        Plant(
            id = "pumpkin",
            thaiName = "ฟักทอง",
            scientificName = "Cucurbita moschata",
            category = PlantCategory.VINE,
            photoRes = com.farmpulse.app.R.drawable.photo_pumpkin,
            description = "ไม้เลื้อยเนื้อที่กว้าง เก็บผลได้ทั้งอ่อนและแก่ ทนแล้งได้ดีกว่าไม้เลื้อยชนิดอื่น",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอกรองพื้นให้หนาก่อนปลูก เพราะเถาแผ่กว้างต้องการธาตุอาหารสะสม",
            references = generalReferences
        ),
        Plant(
            id = "bitter_gourd",
            thaiName = "มะระ",
            scientificName = "Momordica charantia",
            category = PlantCategory.VINE,
            photoRes = com.farmpulse.app.R.drawable.photo_bitter_gourd,
            description = "ไม้เลื้อยรสขม ต้องการค้างให้เกาะ นิยมใช้เป็นทั้งผักและสมุนไพรควบคู่กัน",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 55, moistureMax = 70,
            fertilizerAdvice = "ปุ๋ยหมักช่วงออกดอกติดผล เสริมปุ๋ยน้ำหมักชีวภาพเป็นระยะ",
            references = generalReferences
        ),
        Plant(
            id = "winged_bean",
            thaiName = "ถั่วพู",
            scientificName = "Psophocarpus tetragonolobus",
            category = PlantCategory.VINE,
            photoRes = com.farmpulse.app.R.drawable.photo_winged_bean,
            description = "ไม้เลื้อยตระกูลถั่ว ฝักกรอบมีปีก 4 ด้าน ตรึงไนโตรเจนในดินได้เองเหมือนถั่วฝักยาว",
            phMin = 5.5f, phMax = 6.8f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอกปริมาณน้อยรองพื้นพอ เน้นฟอสฟอรัส-โพแทสเซียมมากกว่าไนโตรเจน",
            references = generalReferences
        ),
        Plant(
            id = "okra",
            thaiName = "กระเจี๊ยบเขียว",
            scientificName = "Abelmoschus esculentus",
            category = PlantCategory.FRUIT_VEG,
            photoRes = com.farmpulse.app.R.drawable.photo_okra,
            description = "ฝักอ่อนเนื้อเมือกลื่น ทนร้อนได้ดีมาก ติดฝักต่อเนื่องถ้าเก็บเกี่ยวสม่ำเสมอไม่ปล่อยให้แก่คาต้น",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอกหรือปุ๋ยหมักช่วงออกดอก ช่วยให้ติดฝักดกต่อเนื่อง",
            references = generalReferences
        ),
        Plant(
            id = "long_eggplant",
            thaiName = "มะเขือยาว",
            scientificName = "Solanum melongena",
            category = PlantCategory.FRUIT_VEG,
            photoRes = com.farmpulse.app.R.drawable.photo_long_eggplant,
            description = "มะเขือผลยาวเนื้อนุ่ม ต้องการแดดเต็มวัน ติดผลต่อเนื่องได้นานหลายเดือนถ้าดูแลดี",
            phMin = 5.5f, phMax = 6.8f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอกรองพื้น เสริมมูลค้างคาวหรือมูลไก่ตากแห้งช่วงติดผล",
            references = generalReferences
        ),
        Plant(
            id = "daikon_radish",
            thaiName = "หัวไชเท้า",
            scientificName = "Raphanus sativus",
            category = PlantCategory.ROOT,
            photoRes = com.farmpulse.app.R.drawable.photo_daikon_radish,
            description = "หัวยาวสีขาวเนื้อกรอบ ชอบดินร่วนลึกไม่มีก้อนหิน จะทำให้หัวโตตรงสวย",
            phMin = 5.8f, phMax = 6.8f,
            moistureMin = 55, moistureMax = 70,
            fertilizerAdvice = "ปุ๋ยคอกเก่ารองพื้น ไม่เน้นไนโตรเจนมากช่วงลงหัวเพราะจะทำให้หัวแตก",
            references = generalReferences
        ),
        Plant(
            id = "corn",
            thaiName = "ข้าวโพด",
            scientificName = "Zea mays",
            category = PlantCategory.FRUIT_VEG,
            photoRes = com.farmpulse.app.R.drawable.photo_corn,
            description = "ต้องการแดดเต็มวันและพื้นที่กว้าง ควรปลูกเป็นแปลงหลายแถวเพื่อให้ผสมเกสรติดฝักดีกว่าปลูกต้นเดียว",
            phMin = 5.8f, phMax = 6.8f,
            moistureMin = 55, moistureMax = 70,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักปริมาณมาก เพราะเป็นพืชที่กินธาตุอาหารสูง",
            references = generalReferences
        ),
        Plant(
            id = "cassava",
            thaiName = "มันสำปะหลัง",
            scientificName = "Manihot esculenta",
            category = PlantCategory.ROOT,
            photoRes = com.farmpulse.app.R.drawable.photo_cassava,
            description = "ทนแล้งได้ดีมาก ปลูกง่ายแทบไม่ต้องดูแล เก็บเกี่ยวหัวได้หลังปลูก 8-12 เดือน",
            phMin = 5.5f, phMax = 6.5f,
            moistureMin = 35, moistureMax = 55,
            fertilizerAdvice = "ปุ๋ยคอกรองพื้นครั้งเดียวตอนปลูกก็เพียงพอตลอดฤดูปลูก",
            references = generalReferences
        ),
        Plant(
            id = "banana",
            thaiName = "กล้วย",
            scientificName = "Musa sapientum",
            category = PlantCategory.TREE,
            photoRes = com.farmpulse.app.R.drawable.photo_banana,
            description = "ไม้ผลยืนต้นให้ผลผลิตต่อเนื่อง ชอบดินชื้นสม่ำเสมอ แตกหน่อขยายกอได้เอง ไม่ต้องปลูกซ้ำ",
            phMin = 5.5f, phMax = 6.5f,
            moistureMin = 60, moistureMax = 75,
            fertilizerAdvice = "ปุ๋ยคอกรอบโคนต้นทุก 2-3 เดือน ช่วยให้แตกหน่อและออกเครือดี",
            references = generalReferences
        ),
        Plant(
            id = "papaya",
            thaiName = "มะละกอ",
            scientificName = "Carica papaya",
            category = PlantCategory.TREE,
            photoRes = com.farmpulse.app.R.drawable.photo_papaya,
            description = "ไม้ผลโตเร็ว ให้ผลได้ตั้งแต่ปีแรก ไม่ทนน้ำขังที่โคนต้นเลย ต้องปลูกในที่ระบายน้ำดีมาก",
            phMin = 6.0f, phMax = 6.5f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักรอบทรงพุ่มทุกเดือน ช่วยให้ติดผลดกต่อเนื่อง",
            references = generalReferences
        ),
        Plant(
            id = "mint",
            thaiName = "สะระแหน่",
            scientificName = "Mentha cordifolia",
            category = PlantCategory.HERB,
            photoRes = com.farmpulse.app.R.drawable.photo_mint,
            description = "ใบกลิ่นหอมเย็น ชอบดินชื้นตลอดเวลา ขยายพันธุ์ง่ายมากด้วยไหลหรือกิ่งปักชำ",
            phMin = 6.0f, phMax = 7.0f,
            moistureMin = 65, moistureMax = 80,
            fertilizerAdvice = "ปุ๋ยหมักเจือจางแต่ใส่บ่อยๆ ดีกว่าใส่มากในครั้งเดียว",
            references = generalReferences
        ),
        Plant(
            id = "coriander",
            thaiName = "ผักชี",
            scientificName = "Coriandrum sativum",
            category = PlantCategory.HERB,
            photoRes = com.farmpulse.app.R.drawable.photo_coriander,
            description = "ใบเล็กกลิ่นฉุน ใช้โรยหน้าอาหารไทยแทบทุกจาน ไม่ทนร้อนจัด ชอบอากาศเย็น",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 55, moistureMax = 70,
            fertilizerAdvice = "ปุ๋ยหมักปริมาณน้อยแต่ใส่ถี่ๆ เพราะรากตื้นดูดซึมได้จำกัด",
            references = generalReferences
        ),
        Plant(
            id = "lemon_basil",
            thaiName = "แมงลัก",
            scientificName = "Ocimum africanum",
            category = PlantCategory.HERB,
            photoRes = com.farmpulse.app.R.drawable.photo_lemon_basil,
            description = "ใบมีขนอ่อนกลิ่นหอมอ่อนกว่ากะเพรา นิยมใส่แกงเลียง ปลูกดูแลคล้ายกะเพรา",
            phMin = 6.0f, phMax = 7.0f,
            moistureMin = 45, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอกหรือปุ๋ยหมัก ใส่ทุก 2-3 สัปดาห์",
            references = generalReferences
        ),
        Plant(
            id = "turmeric",
            thaiName = "ขมิ้น",
            scientificName = "Curcuma longa",
            category = PlantCategory.ROOT,
            photoRes = com.farmpulse.app.R.drawable.photo_turmeric,
            description = "เหง้าสีเหลืองส้ม ใช้ทั้งเป็นเครื่องเทศและสมุนไพร ชอบร่มเงาบางส่วนมากกว่าแดดจัดทั้งวัน",
            phMin = 5.5f, phMax = 6.5f,
            moistureMin = 55, moistureMax = 70,
            fertilizerAdvice = "ปุ๋ยคอกรองพื้นให้หนาก่อนปลูก เพราะต้องสะสมอาหารในเหง้านาน",
            references = generalReferences
        ),
        Plant(
            id = "fingerroot",
            thaiName = "กระชาย",
            scientificName = "Boesenbergia rotunda",
            category = PlantCategory.ROOT,
            photoRes = com.farmpulse.app.R.drawable.photo_fingerroot,
            description = "เหง้าลักษณะคล้ายนิ้วมือ กลิ่นฉุนกว่าขมิ้น ใช้แต่งกลิ่นแกงป่าและเป็นสมุนไพร",
            phMin = 5.5f, phMax = 6.5f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักช่วงแตกหน่อใหม่",
            references = generalReferences
        ),
        Plant(
            id = "centella",
            thaiName = "บัวบก",
            scientificName = "Centella asiatica",
            category = PlantCategory.HERB,
            photoRes = com.farmpulse.app.R.drawable.photo_centella,
            description = "ใบกลมเล็กเลื้อยไปตามพื้น ชอบที่ชื้นแฉะ ปลูกในร่มเงาได้ดีกว่าที่แดดจัด",
            phMin = 5.5f, phMax = 7.0f,
            moistureMin = 65, moistureMax = 85,
            fertilizerAdvice = "ปุ๋ยคอกปริมาณน้อย ไม่ต้องการธาตุอาหารมาก",
            references = generalReferences
        ),
        Plant(
            id = "wild_betel",
            thaiName = "ชะพลู",
            scientificName = "Piper sarmentosum",
            category = PlantCategory.HERB,
            photoRes = com.farmpulse.app.R.drawable.photo_wild_betel,
            description = "ใบรูปหัวใจกลิ่นเฉพาะตัว ใช้ห่อเมี่ยงคำหรือใส่แกง ชอบร่มเงาและดินชื้นสม่ำเสมอ",
            phMin = 6.0f, phMax = 7.0f,
            moistureMin = 60, moistureMax = 75,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักทุกเดือน",
            references = generalReferences
        ),
        Plant(
            id = "pandan",
            thaiName = "เตย",
            scientificName = "Pandanus amaryllifolius",
            category = PlantCategory.HERB,
            photoRes = com.farmpulse.app.R.drawable.photo_pandan,
            description = "ใบยาวกลิ่นหอมหวาน ใช้แต่งกลิ่นและสีขนมไทย ชอบดินชื้นสม่ำเสมอ ไม่ทนแล้งเลย",
            phMin = 5.5f, phMax = 6.5f,
            moistureMin = 65, moistureMax = 80,
            fertilizerAdvice = "ปุ๋ยคอกรอบกอทุก 2 เดือน",
            references = generalReferences
        ),
        Plant(
            id = "kaffir_lime",
            thaiName = "มะกรูด",
            scientificName = "Citrus hystrix",
            category = PlantCategory.TREE,
            photoRes = com.farmpulse.app.R.drawable.photo_kaffir_lime,
            description = "ไม้ยืนต้นปลูกไว้เก็บใบและผิวผลใช้แต่งกลิ่นอาหารไทย ทนแล้งได้ดีกว่ามะนาว",
            phMin = 6.0f, phMax = 6.5f,
            moistureMin = 45, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักรอบทรงพุ่มปีละ 2-3 ครั้ง",
            references = generalReferences
        ),
        Plant(
            id = "marigold",
            thaiName = "ดาวเรือง",
            scientificName = "Tagetes erecta",
            category = PlantCategory.FLOWER,
            photoRes = com.farmpulse.app.R.drawable.photo_marigold,
            description = "ดอกสีเหลือง-ส้มสด ปลูกง่ายทนแดด นิยมปลูกแซมแปลงผักเพื่อไล่แมลงศัตรูพืชแบบธรรมชาติ",
            phMin = 6.0f, phMax = 7.5f,
            moistureMin = 45, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักทุก 3-4 สัปดาห์ ช่วยให้ออกดอกดกต่อเนื่อง",
            references = generalReferences
        ),
        Plant(
            id = "jasmine",
            thaiName = "มะลิ",
            scientificName = "Jasminum sambac",
            category = PlantCategory.FLOWER,
            photoRes = com.farmpulse.app.R.drawable.photo_jasmine,
            description = "ดอกสีขาวกลิ่นหอม เป็นสัญลักษณ์วันแม่ของไทย ชอบแดดจัดอย่างน้อยครึ่งวัน",
            phMin = 6.0f, phMax = 7.0f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอกรอบโคนต้นทุกเดือน ช่วยให้แตกยอดออกดอกใหม่สม่ำเสมอ",
            references = generalReferences
        ),
        Plant(
            id = "butterfly_pea",
            thaiName = "อัญชัน",
            scientificName = "Clitoria ternatea",
            category = PlantCategory.FLOWER,
            photoRes = com.farmpulse.app.R.drawable.photo_butterfly_pea,
            description = "ไม้เลื้อยดอกสีน้ำเงินม่วง ใช้ทำสีผสมอาหาร/เครื่องดื่มธรรมชาติ ตรึงไนโตรเจนได้เองเหมือนพืชตระกูลถั่ว",
            phMin = 5.5f, phMax = 7.0f,
            moistureMin = 45, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอกปริมาณน้อย ไม่ต้องการไนโตรเจนมากเพราะตรึงได้เอง",
            references = generalReferences
        ),
        Plant(
            id = "sunflower",
            thaiName = "ทานตะวัน",
            scientificName = "Helianthus annuus",
            category = PlantCategory.FLOWER,
            photoRes = com.farmpulse.app.R.drawable.photo_sunflower,
            description = "ดอกใหญ่หันตามแสงอาทิตย์ ต้องการแดดเต็มวันและพื้นที่โล่งไม่บังแสง",
            phMin = 6.0f, phMax = 7.5f,
            moistureMin = 45, moistureMax = 60,
            fertilizerAdvice = "ปุ๋ยคอก/ปุ๋ยหมักรองพื้นก่อนปลูกให้เพียงพอ",
            references = generalReferences
        ),
        Plant(
            id = "rose",
            thaiName = "กุหลาบ",
            scientificName = "Rosa hybrida",
            category = PlantCategory.FLOWER,
            photoRes = com.farmpulse.app.R.drawable.photo_rose,
            description = "ไม้พุ่มดอกหอมมีหนาม ต้องการแดดจัดและตัดแต่งกิ่งสม่ำเสมอเพื่อให้ออกดอกต่อเนื่อง",
            phMin = 6.0f, phMax = 6.8f,
            moistureMin = 50, moistureMax = 65,
            fertilizerAdvice = "ปุ๋ยคอก/มูลไก่รอบโคนต้นทุกเดือน",
            references = generalReferences
        ),
        Plant(
            id = "orchid",
            thaiName = "กล้วยไม้",
            scientificName = "Orchidaceae",
            category = PlantCategory.FLOWER,
            photoRes = com.farmpulse.app.R.drawable.photo_orchid,
            description = "ไม้ดอกเกาะอาศัย ไม่ปลูกในดินธรรมดา ต้องใช้วัสดุปลูกโปร่ง เช่น ถ่านหรือรากเฟิร์น รดน้ำแต่ไม่แฉะ",
            phMin = 5.5f, phMax = 6.5f,
            moistureMin = 55, moistureMax = 70,
            fertilizerAdvice = "ปุ๋ยสูตรเสมอละลายน้ำฉีดพ่นทุก 1-2 สัปดาห์ ใช้ความเข้มข้นเจือจางกว่าพืชทั่วไป",
            references = generalReferences
        ),    )

    /**
     * เรียงรายชื่อพืชตามลำดับตัวอักษรไทย ก-ฮ ให้ถูกต้องตามหลักการเรียงคำภาษาไทยจริง
     * (ใช้ Collator แทนการเรียงตาม Unicode ตรงๆ เพราะสระนำหน้าอย่าง เ-, แ-, โ-, ใ-, ไ- ต้องถูกจัดให้เรียง
     * ตามพยัญชนะที่ตามหลัง ไม่ใช่เรียงตามตำแหน่งสระที่อยู่หน้าคำในการเขียน)
     */
    val all: List<Plant> = allUnsorted.sortedWith(
        compareBy(java.text.Collator.getInstance(java.util.Locale("th", "TH"))) { it.thaiName }
    )
}
