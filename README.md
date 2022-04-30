# Yemekhane Rezervasyon Sistemi

Bu proje okul yemekhanesi düşünülerek hazırlanmış bir rezervasyon yönetim
sistemidir. İstemci JavaFX kullanılarak geliştirilmiştir.

Projeyi üniversitenin başlarında kodlamıştım. Elimde bu *codebase*ten başka
döküman olmadığı için kodun ne yaptığını şimdi, 4 yılın ardından tersine
mühendislik yaparak anlamaya çalışacağım.

Önce veritabanı şemasını çıkarmaya karar verdim. Uygulamanın rastgele
yerlerindeki SQL kodlarını inceleyerek çıkardığım tablo yapıları şu şekilde::

```sql
CREATE TABLE `users` (
    `pid` int NOT NULL,
    `firstname` varchar(50) NOT NULL,
    `lastname` varchar(50) NOT NULL,
    `password` varchar(50) NOT NULL,
    PRIMARY KEY (`pid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;


CREATE TABLE `meal` (
    `mid` INT(10) NOT NULL AUTO_INCREMENT,
    `date` DATE NOT NULL,
    `repast` ENUM('B','L','D') NOT NULL,
    PRIMARY KEY (`mid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
AUTO_INCREMENT=2
;


CREATE TABLE `food` (
    `fid` INT(10) NOT NULL AUTO_INCREMENT,
    `food_name` VARCHAR(50) NOT NULL,
    PRIMARY KEY (`fid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
AUTO_INCREMENT=11
;


CREATE TABLE `reservations` (
    `pid` INT(10) NOT NULL,
    `mid` INT(10) NOT NULL,
    `refectory` ENUM('ieylul','yemre') NOT NULL,
    PRIMARY KEY (`pid`, `mid`),
    INDEX `FK_reservations_meal` (`mid`),
    CONSTRAINT `FK_reservations_meal` FOREIGN KEY (`mid`) REFERENCES `meal` (`mid`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `FK_reservations_users` FOREIGN KEY (`pid`) REFERENCES `users` (`pid`) ON UPDATE CASCADE ON DELETE RESTRICT
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;


CREATE TABLE `purchase` (
    `pid` INT(10) NOT NULL,
    `mid` INT(10) NOT NULL,
    `date` DATE NOT NULL,
    `repast` ENUM('B','L','D') NOT NULL,
    `refectory` ENUM('ieylul','yemre') NOT NULL,
    PRIMARY KEY (`pid`, `mid`, `date`),
    INDEX `FK_purchase_meal` (`mid`),
    CONSTRAINT `FK_purchase_meal` FOREIGN KEY (`mid`) REFERENCES `meal` (`mid`) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT `FK_purchase_users` FOREIGN KEY (`pid`) REFERENCES `users` (`pid`) ON UPDATE CASCADE ON DELETE RESTRICT
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;


CREATE TABLE `has_food` (
    `fid` INT(10) NOT NULL,
    `mid` INT(10) NOT NULL,
    PRIMARY KEY (`fid`, `mid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;


CREATE TABLE `has_meal` (
    `pid` INT(10) NOT NULL,
    `mid` INT(10) NOT NULL,
    `refectory` ENUM('ieylul','yemre') NOT NULL,
    PRIMARY KEY (`pid`, `mid`)
)
COLLATE='utf8_turkish_ci'
ENGINE=InnoDB
;
```

Uygulama üzerinde bir geliştirme yapmadan önce şu haliyle çalışıp çalışmadığını
test etmeye karar verdim. İlk yapacağım iş, test verileri oluşturan bir betik
yazmak oldu. (`org.bozdgn.CreateMockDatabase.java`) Bu betik<!-- ya da "script", her neyse artık.. --> veritabanını sıfırlayıp tabloları baştan oluşturuyor ve istemcinin çalışabilmesi
için birkaç örnek veri oluşturuyor. 

