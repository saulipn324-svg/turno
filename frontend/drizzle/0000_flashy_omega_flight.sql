CREATE TABLE `bookings` (
	`id` text PRIMARY KEY NOT NULL,
	`room` text NOT NULL,
	`date` text NOT NULL,
	`start` integer NOT NULL,
	`duration` integer NOT NULL,
	`title` text NOT NULL,
	`name` text NOT NULL,
	`status` text DEFAULT 'CONFIRMED' NOT NULL
);
--> statement-breakpoint
CREATE INDEX `idx_bookings_date` ON `bookings` (`date`);--> statement-breakpoint
CREATE TABLE `slots` (
	`id` text PRIMARY KEY NOT NULL,
	`booking_id` text NOT NULL,
	`room` text NOT NULL,
	`date` text NOT NULL,
	`start` integer NOT NULL,
	FOREIGN KEY (`booking_id`) REFERENCES `bookings`(`id`) ON UPDATE no action ON DELETE no action
);
--> statement-breakpoint
CREATE UNIQUE INDEX `uq_room_date_start` ON `slots` (`room`,`date`,`start`);