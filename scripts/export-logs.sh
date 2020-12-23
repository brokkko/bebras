#!/bin/bash
mongoexport --collection='contest-koza20_task1-ru' --db=dces2 --host=10.146.2.21 --out='task1-ru.json'
mongoexport --collection='contest-koza20_task2-ru' --db=dces2 --host=10.146.2.21 --out='task2-ru.json'
mongoexport --collection='contest-koza20_task3-ru' --db=dces2 --host=10.146.2.21 --out='task3-ru.json'
mongoexport --collection='contest-koza20_task4-ru' --db=dces2 --host=10.146.2.21 --out='task4-ru.json'
mongoexport --collection='contest-koza20_task5-ru' --db=dces2 --host=10.146.2.21 --out='task5-ru.json'

mongoexport --collection='contest-koza20_task1-en' --db=dces2 --host=10.146.2.21 --out='task1-en.json'
mongoexport --collection='contest-koza20_task2-en' --db=dces2 --host=10.146.2.21 --out='task2-en.json'
mongoexport --collection='contest-koza20_task3-en' --db=dces2 --host=10.146.2.21 --out='task3-en.json'
mongoexport --collection='contest-koza20_task4-en' --db=dces2 --host=10.146.2.21 --out='task4-en.json'
mongoexport --collection='contest-koza20_task5-en' --db=dces2 --host=10.146.2.21 --out='task5-en.json'

mongoexport --db=dces2 --host=10.146.2.21 --out=wordsStream.forEach(word -> System.out.println(word));

        //больше ничего с этим потоком уже не сделать
//        wordsStream.forEach(word -> System.out.println(word.length()));
        // Ошибка!! нельзя второй раз перебрать элементы потока.

        Stream<Integer> naturals = Stream.iterate(1, x -> x + 1);
        //поток натуральных чисел. Сначала 1, каждое следующее - это предыдущее плюс 1

        naturals
                .takeWhile(x -> x < 100) //получаем другой поток, в котором
                //элементы идут только пока они меньше 10
                .forEach(num -> System.out.println(num));
    }'activity.json' --collection=activity --query='{u: {$in: [ObjectId("5fbb76c298ec00ea7e9f0f5e"), ObjectId("5fbb76c298ec00ea7e9f0f5f"), ObjectId("5fbb76c398ec00ea7e9f0f60"), ObjectId("5fbb76c398ec00ea7e9f0f61"), ObjectId("5fbb76c498ec00ea7e9f0f79"), ObjectId("5fbb76c498ec00ea7e9f0f7a"), ObjectId("5fbb76c498ec00ea7e9f0f7b"), ObjectId("5fbb76c498ec00ea7e9f0f7c"), ObjectId("5fbb76c798ec00ea7e9f0f9c"), ObjectId("5fbb76c798ec00ea7e9f0f9d"), ObjectId("5fbb76c898ec00ea7e9f0f9e"), ObjectId("5fbb76c898ec00ea7e9f0f9f"), ObjectId("5fbb76c998ec00ea7e9f0fd0"), ObjectId("5fbb76c998ec00ea7e9f0fd1"), ObjectId("5fbb76c998ec00ea7e9f0fd2"), ObjectId("5fbb76c998ec00ea7e9f0fd3"), ObjectId("5fbb76c998ec00ea7e9f0fd4"), ObjectId("5fbb76c998ec00ea7e9f0fd5"), ObjectId("5fbb76ca98ec00ea7e9f0fd6"), ObjectId("5fbb76ca98ec00ea7e9f0fd7"), ObjectId("5fbb76ca98ec00ea7e9f0fd8"), ObjectId("5fbb76ca98ec00ea7e9f0fd9"), ObjectId("5fbb76ca98ec00ea7e9f0fda"), ObjectId("5fbb76ca98ec00ea7e9f0fdb"), ObjectId("5fbb76cb98ec00ea7e9f0fe2"), ObjectId("5fbb76cb98ec00ea7e9f0fe3"), ObjectId("5fbb76cb98ec00ea7e9f0fe4"), ObjectId("5fbb76cb98ec00ea7e9f0fe5"), ObjectId("5fbb76cc98ec00ea7e9f0fee"), ObjectId("5fbb76cc98ec00ea7e9f0fef"), ObjectId("5fbb76cc98ec00ea7e9f0ff0"), ObjectId("5fbb76cc98ec00ea7e9f0ff1"), ObjectId("5fbb76ce98ec00ea7e9f1005"), ObjectId("5fbb76ce98ec00ea7e9f1006"), ObjectId("5fbb76ce98ec00ea7e9f1007"), ObjectId("5fbb76ce98ec00ea7e9f1008"), ObjectId("5fbb76cf98ec00ea7e9f1015"), ObjectId("5fbb76cf98ec00ea7e9f1016"), ObjectId("5fbb76cf98ec00ea7e9f1017"), ObjectId("5fbb76cf98ec00ea7e9f1018"), ObjectId("5fbb76c398ec00ea7e9f0f75"), ObjectId("5fbb76c398ec00ea7e9f0f76"), ObjectId("5fbb76c398ec00ea7e9f0f77"), ObjectId("5fbb76c498ec00ea7e9f0f78"), ObjectId("5fbb76c498ec00ea7e9f0f7d"), ObjectId("5fbb76c598ec00ea7e9f0f7e"), ObjectId("5fbb76c598ec00ea7e9f0f7f"), ObjectId("5fbb76c598ec00ea7e9f0f80"), ObjectId("5fbb76c598ec00ea7e9f0f81"), ObjectId("5fbb76c598ec00ea7e9f0f82"), ObjectId("5fbb76c598ec00ea7e9f0f83"), ObjectId("5fbb76c598ec00ea7e9f0f84"), ObjectId("5fbb76c598ec00ea7e9f0f85"), ObjectId("5fbb76c598ec00ea7e9f0f89"), ObjectId("5fbb76c598ec00ea7e9f0f8a"), ObjectId("5fbb76c698ec00ea7e9f0f8b"), ObjectId("5fbb76c698ec00ea7e9f0f8c"), ObjectId("5fbb76c698ec00ea7e9f0f8d"), ObjectId("5fbb76c698ec00ea7e9f0f8e"), ObjectId("5fbb76c698ec00ea7e9f0f8f"), ObjectId("5fbb76c698ec00ea7e9f0f90"), ObjectId("5fbb76c698ec00ea7e9f0f91"), ObjectId("5fbb76c698ec00ea7e9f0f92"), ObjectId("5fbb76c698ec00ea7e9f0f93"), ObjectId("5fbb76c698ec00ea7e9f0f94"), ObjectId("5fbb76c798ec00ea7e9f0f95"), ObjectId("5fbb76c798ec00ea7e9f0f96"), ObjectId("5fbb76c798ec00ea7e9f0f97"), ObjectId("5fbb76c798ec00ea7e9f0f98"), ObjectId("5fbb76c798ec00ea7e9f0f99"), ObjectId("5fbb76c798ec00ea7e9f0f9a"), ObjectId("5fbb76c798ec00ea7e9f0f9b"), ObjectId("5fbb76c898ec00ea7e9f0fa0"), ObjectId("5fbb76c898ec00ea7e9f0fab"), ObjectId("5fbb76c898ec00ea7e9f0fce"), ObjectId("5fbb76c998ec00ea7e9f0fcf"), ObjectId("5fbb76ca98ec00ea7e9f0fdc"), ObjectId("5fbb76ca98ec00ea7e9f0fdf"), ObjectId("5fbb76ca98ec00ea7e9f0fe0"), ObjectId("5fbb76cb98ec00ea7e9f0fe1"), ObjectId("5fbb76cb98ec00ea7e9f0fe6"), ObjectId("5fbb76cb98ec00ea7e9f0fe7"), ObjectId("5fbb76cb98ec00ea7e9f0fe8"), ObjectId("5fbb76cb98ec00ea7e9f0fe9"), ObjectId("5fbb76cb98ec00ea7e9f0fea"), ObjectId("5fbb76cc98ec00ea7e9f0feb"), ObjectId("5fbb76cc98ec00ea7e9f0fec"), ObjectId("5fbb76cc98ec00ea7e9f0fed"), ObjectId("5fbb76cc98ec00ea7e9f0ff2"), ObjectId("5fbb76cc98ec00ea7e9f0ff3"), ObjectId("5fbb76cc98ec00ea7e9f0ff7"), ObjectId("5fbb76cc98ec00ea7e9f0ff8"), ObjectId("5fbb76cd98ec00ea7e9f0ff9"), ObjectId("5fbb76cd98ec00ea7e9f0ffa"), ObjectId("5fbb76cd98ec00ea7e9f0ffb"), ObjectId("5fbb76cd98ec00ea7e9f0ffc"), ObjectId("5fbb76cd98ec00ea7e9f0ffd"), ObjectId("5fbb76cd98ec00ea7e9f0ffe"), ObjectId("5fbb76cd98ec00ea7e9f0fff"), ObjectId("5fbb76cd98ec00ea7e9f1000"), ObjectId("5fbb76cd98ec00ea7e9f1001"), ObjectId("5fbb76cd98ec00ea7e9f1002"), ObjectId("5fbb76cd98ec00ea7e9f1003"), ObjectId("5fbb76ce98ec00ea7e9f1004"), ObjectId("5fbb76ce98ec00ea7e9f1009"), ObjectId("5fbb76ce98ec00ea7e9f100a"), ObjectId("5fbb76ce98ec00ea7e9f100b"), ObjectId("5fbb76ce98ec00ea7e9f100c"), ObjectId("5fbb76ce98ec00ea7e9f100d"), ObjectId("5fbb76ce98ec00ea7e9f100e"), ObjectId("5fbb76cf98ec00ea7e9f100f"), ObjectId("5fbb76cf98ec00ea7e9f1010"), ObjectId("5fbb76cf98ec00ea7e9f1011"), ObjectId("5fbb76cf98ec00ea7e9f1012"), ObjectId("5fbb76cf98ec00ea7e9f1013"), ObjectId("5fbb76cf98ec00ea7e9f1014"), ObjectId("5fbb76cf98ec00ea7e9f1019"), ObjectId("5fbb76d098ec00ea7e9f101a"), ObjectId("5fbb76d098ec00ea7e9f101b"), ObjectId("5fbb76d098ec00ea7e9f101c"), ObjectId("5fbb76d098ec00ea7e9f101d"), ObjectId("5fbb76d098ec00ea7e9f1020"), ObjectId("5fbb76d098ec00ea7e9f1021"), ObjectId("5fbb76d098ec00ea7e9f1022"), ObjectId("5fbb76d098ec00ea7e9f1023"), ObjectId("5fbb76d098ec00ea7e9f1024"), ObjectId("5fbb76d098ec00ea7e9f1025"), ObjectId("5fbb76d098ec00ea7e9f1026"), ObjectId("5fbb76d198ec00ea7e9f1027"), ObjectId("5fbb76d198ec00ea7e9f1028"), ObjectId("5fbb76d198ec00ea7e9f1029"), ObjectId("5fbb76d198ec00ea7e9f102a")]}}'