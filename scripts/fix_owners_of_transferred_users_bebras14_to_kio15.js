logins = ['Vaniushkina', 'Ylia_Danilchenko', 'School33', 'school639', 'inn428', 'ugg', 'laptev777andrey', 'Driamy', 'boldireva_nata', 'inna-uzunyan', 'nyu', 'helpgym155', 'LaktionovaOA', 'Nastin', 'Tatiana1984', 'Danila', 'oakudro', 'pennat', 'elengusarova', 'nikolai58', 'Lena_Pro', 'aea1980', 'spb-school286', 'poxana', 'Bandurovasveta', 'girafenok', 'ged', 'annatimofeevakmkk', 'pangina', 'andronova', 'alexmind', 'marina_f', 'Hakasiy_BYr_2014', 'mcafe', 'Sayyora', 'ROZENNV', 'vad_varlamov', 'vfrc_vbif', 'ivstepanova', 'klints-ova', 'sch13vorkuta', 'Anapa4', 'nika8', 'guvm', 'sgavrilina', 'gimn8tv', 'sidortsova', 'Chechrus', '1q2w3e', 'kev-tomsk', 'busnya', 'prizrakss', 'ikt', 'TrofimenkoEG', 'ehavkina', 'IRENV', 'may', 'imakshakov', 'danilaeg', 'valpalbal', 'Ivannik', '210danily', '1234', 'Roomull', 'inforMatik', 'golubinka', 'metro', 'Marina_Zaw', 'lyudanovikova', 'veako', 'antikakos', 'Zuboa', 'Severyanka86', 'vissa', 'svetlanazh', 'Fedor', 'liz71', 'mousos10', 'my0707', 'somlich', 'alen2821', 'kadkorkomp', 'tonm', 'makin', 'latch51', 'Dudnik511', 'Mamaya', 'mazhova47', 'Anoprienko', 'eue24', 'pvnikolaenko', 'korochenko', 'elenapaus', 'Sirena', 'serovala', 'isvet13', 'delo-72', 'otrad-plodovoe', 'mary-nota', 'polna09', 'lisanna', 'Nutik_an', 'spb-school259', 'nveg08', 'irinamir', 'kotik1406', 'romanchugova', 'ann_georg', 'tanuxa', 'infoteacher', 'Chelondaev', 'OlgaK', 'gimnaziya50', 'InnaVSch', 'valentinka', 'lepaeva', 'Ekaterina89', 'smirnova', 'troitskaya', 'school314', 'maruha', 'lazanat', 'Space', 'sch113prim', 'akmaiva', 'pletnevatana', 'zinovieva', 'avi', 'kubaytonya', 'RomanZharkov', 'kutepova_nv', 'katinshkola', 'LubLeo', 'glebgor', 'katy526', 'zatynaychenko', '3cl_ass-B1', 'vasirina', 'dnepr_1972', 'verastsig', 'infoteacher366', 'EskaA', 'nvxrust', 'mashchis', 'dimasmirnov', 'klinkovskayamv', 'spb-sec-gymn', 'AntonovaLS', 'umniki12', 'Kurets', 'Lara_b70', 'dvnata', 'm17a2008', 'xoxlova_nvkz', 'vgavina', 'marybes', 'lisenok-t2006', 'gagatata', '12_gimn_tver', 'pk31-spvavilovskoe', 'annik83', 'natali1961', 'eklenkina', 'ryavin_86', 'alisaev', 'vika_ross', 'skidanova_ea', 'sykt_kbotalov', 'NataliK', 'Luciya', 'bistrova75', 'Parysova', 'evgenija1979', 'dzhukashev'];

logins.map(function(login) {
    db.users.find({event_id: "bebras14", login: login}).forEach(function(user) {
        var uid = user._id;
        var reg_by = user._reg_by;
        if (reg_by.length > 0) {
            var owner = db.users.findOne({_id: reg_by[0]});
            print(login + ";" + uid + ";" + owner.login + ";" + owner._id);
        }
    });
});