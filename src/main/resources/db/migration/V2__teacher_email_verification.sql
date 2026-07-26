ALTER TABLE teacher_user_details 
ADD COLUMN isVerified BIT(1) NOT NULL DEFAULT TRUE, 
ADD COLUMN verificationCode VARCHAR(255) NULL;
