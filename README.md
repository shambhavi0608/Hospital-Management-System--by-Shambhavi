# Smart Hospital Management System

A Java Swing + MySQL desktop application with a modern role-based interface.

## Implemented modules

- Secure database login with automatic ADMIN, DOCTOR, NURSE or PATIENT role detection
- Role-specific navigation and patient-only portal
- Live dashboard cards for patients, rooms, doctors and nurses
- Patient admission, search, update and safe discharge transactions
- Admin-only doctor/nurse management with hospital department dropdown
- Smart room finder
- Live analytics with date filters, donut charts, trend chart and room-type chart
- Multilingual text/voice assistant
- Consistent vector icons and custom-painted coloured buttons (no Windows grey fallback)

## Run in IntelliJ IDEA

1. Open this project folder in IntelliJ IDEA.
2. Confirm the bundled MySQL connector JAR is on the module classpath.
3. Open **Run > Edit Configurations > Login**.
4. Add environment variable `HMS_DB_PASSWORD=YOUR_MYSQL_PASSWORD`.
5. Make sure MySQL is running and database `hospital_management_system` exists.
6. Run `hospital.management.system.Login`.

For microphone input, enable Windows microphone permission and install at least
one Windows Speech language. The app prefers the selected language and safely
falls back to an installed `en-IN`, `en-US`, or first available recognizer.

## Role data expected in MySQL

The `login` table must contain `ID`, `PW`, `Role` and `Patient_Number`.
Supported roles are `ADMIN`, `DOCTOR`, `NURSE` and `PATIENT`.
For a patient login, `Patient_Number` must match `Patient_Info.Number`.

Doctors and nurses are stored in `staff_info`. Inactive staff accounts are blocked
from logging in. Only an authenticated administrator can add staff or change staff
status.

## Validation

- Aadhaar: exactly 12 digits
- Indian mobile: exactly 10 digits beginning with 6-9; optional `+91` prefix
- Email: valid email format
- Departments: selected from a hospital-specific dropdown

Passwords are read from the database used by this original academic project. For a
production deployment, migrate the `PW` column to a strong password hash such as
Argon2id or bcrypt.
