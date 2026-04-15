INSERT INTO employees (first_name,last_name,email,department,title,hire_date,is_active) VALUES
('Avery','Nguyen','avery.nguyen@contoso.example','Engineering','Software Engineer','2019-04-10',1),
('Mateo','Rivera','mateo.rivera@contoso.example','Engineering','QA Engineer','2020-02-12',1),
('Priya','Patel','priya.patel@contoso.example','Finance','Analyst','2018-03-21',1),
('Jordan','Kim','jordan.kim@contoso.example','Marketing','Coordinator','2021-06-14',1),
('Fatima','Hassan','fatima.hassan@contoso.example','Human Resources','HR Manager','2017-09-30',1),
('Noah','Johnson','noah.johnson@contoso.example','Operations','Ops Specialist','2016-11-02',0),
('Lucia','Garcia','lucia.garcia@contoso.example','Sales','Account Executive','2022-01-05',1),
('Samuel','Okoro','samuel.okoro@contoso.example','Engineering','DevOps Engineer','2015-07-19',1),
('Mei','Chen','mei.chen@contoso.example','Finance','Controller','2014-08-25',1),
('Elias','Ibrahim','elias.ibrahim@contoso.example','Operations','Warehouse Lead','2013-10-11',1),
('Amara','Diallo','amara.diallo@contoso.example','Sales','Sales Manager','2012-12-01',0),
('Sofia','Rossi','sofia.rossi@contoso.example','Marketing','Designer','2023-02-17',1);

INSERT INTO assets (asset_tag,asset_type,manufacturer,model,serial_number,purchase_date,warranty_expiry,status,notes) VALUES
('CON-LPT-001','Laptop','Dell','Latitude 7420','SN10001','2022-03-01','2025-03-01','assigned','Primary engineering laptop'),
('CON-LPT-002','Laptop','DELL','Latitude 7420',NULL,'2022-03-05','2025-03-05','available','Missing serial in legacy entry'),
('CON-LPT-003','Laptop','Dell Inc.','Latitude 7430','SN10003','2023-01-10','2026-01-10','assigned',''),
('CON-MON-001','Monitor','Samsung','S24','MN20001','2021-05-11','2024-05-11','available',''),
('CON-MON-002','Monitor','LG','27UL500','MN20002','2020-10-13','2023-10-13','retired','Flickering screen'),
('CON-MON-003','Monitor','HP','E24','MN20003','2022-09-09','2025-09-09','assigned',''),
('CON-KBD-001','Keyboard','Logitech','K120','KB30001','2019-07-07','2022-07-07','available',''),
('CON-KBD-002','Keyboard','Logitech','MX Keys','KB30002','2022-12-01','2025-12-01','assigned',''),
('CON-PHN-001','Phone','Cisco','CP-8841','PH40001','2018-01-20','2021-01-20','retired','Legacy desk phone'),
('CON-PHN-002','Phone','Poly','VVX 450','PH40002','2023-04-15','2026-04-15','available',''),
('CON-BDG-001','Keycard/Badge','HID','ProxCard II',NULL,'2021-02-02','2026-02-02','assigned','Badge issued to inactive employee'),
('CON-BDG-002','Keycard/Badge','HID','ProxCard II','BD50002','2022-02-02','2027-02-02','available',''),
('CON-DCK-001','Docking Station','Dell','WD19','DK60001','2022-05-18','2025-05-18','assigned',''),
('CON-DCK-002','Docking Station','Lenovo','ThinkPad Dock','DK60002','2020-05-18','2023-05-18','lost','Not found after office move');

INSERT INTO assignment_history (asset_id,employee_id,assigned_date,returned_date,assigned_by,notes) VALUES
(1,1,'2024-01-04',NULL,'it-admin','Current assignment'),
(3,8,'2024-02-12',NULL,'it-admin','Current assignment'),
(6,7,'2024-01-02',NULL,'it-admin','Current assignment'),
(8,2,'2024-03-11',NULL,'it-admin','Current assignment'),
(11,6,'2024-02-01',NULL,'it-admin','Assigned to inactive employee (intentional bug seed)'),
(13,1,'2024-01-04',NULL,'it-admin','Current assignment'),
(2,4,'2023-01-10','2023-11-01','it-admin','Returned after role change'),
(4,9,'2022-08-03','2023-09-18','it-admin','Replaced with newer monitor');
