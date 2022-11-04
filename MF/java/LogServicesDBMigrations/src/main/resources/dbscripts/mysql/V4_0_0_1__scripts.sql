ALTER TABLE `customeractivity` 
ADD COLUMN `sessionId` VARCHAR(100) NULL AFTER `id`,
ADD COLUMN `deviceId` VARCHAR(100) NULL AFTER `device`,
ADD COLUMN `browser` VARCHAR(50) NULL AFTER `operatingSystem`;