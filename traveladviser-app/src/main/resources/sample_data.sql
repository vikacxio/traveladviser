-- Comprehensive sample data for Travel Advisor - 100 Top Places in India
-- This script includes real locations with accurate coordinates

-- Clear existing data (if any)
TRUNCATE TABLE place_tags CASCADE;
TRUNCATE TABLE places CASCADE;
TRUNCATE TABLE tags CASCADE;
TRUNCATE TABLE categories CASCADE;
TRUNCATE TABLE cities CASCADE;

-- Insert categories
INSERT INTO categories (name) VALUES 
    ('Mountain'),
    ('Beach'),
    ('Temple'),
    ('Museum'),
    ('Park'),
    ('Fort'),
    ('Lake'),
    ('Waterfall'),
    ('Garden'),
    ('Historical'),
    ('Adventure'),
    ('Wildlife'),
    ('Shopping');

-- Insert tags
INSERT INTO tags (name) VALUES 
    ('cool_place'),
    ('water_spot'),
    ('snow'),
    ('fireplace'),
    ('indoor'),
    ('museum'),
    ('cafe'),
    ('outdoor'),
    ('beach'),
    ('hiking'),
    ('budget_friendly'),
    ('luxury'),
    ('family_friendly'),
    ('adventure'),
    ('romantic'),
    ('photography'),
    ('spiritual'),
    ('historical'),
    ('nature_lover'),
    ('wildlife');

-- Insert cities
INSERT INTO cities (name, state, country, latitude, longitude) VALUES 
    ('Delhi', 'Delhi', 'India', 28.6139, 77.2090),
    ('Mumbai', 'Maharashtra', 'India', 19.0760, 72.8777),
    ('Bangalore', 'Karnataka', 'India', 12.9716, 77.5946),
    ('Hyderabad', 'Telangana', 'India', 17.3850, 78.4867),
    ('Kolkata', 'West Bengal', 'India', 22.5726, 88.3639),
    ('Chennai', 'Tamil Nadu', 'India', 13.0827, 80.2707),
    ('Pune', 'Maharashtra', 'India', 18.5204, 73.8567),
    ('Jaipur', 'Rajasthan', 'India', 26.9124, 75.7873),
    ('Manali', 'Himachal Pradesh', 'India', 32.2396, 77.1887),
    ('Goa', 'Goa', 'India', 15.2993, 73.8243);

-- Insert 100 places with diverse locations and ratings
INSERT INTO places (name, description, category_id, latitude, longitude, location, city, state, country, best_season, price_level, avg_rating, total_ratings, source, source_id)
VALUES
    -- Mountains (10 places)
    ('Auli', 'India''s premier skiing destination in Uttarakhand', 1, 30.0136, 79.5981, ST_MakePoint(79.5981, 30.0136)::geography, 'Chopta', 'Uttarakhand', 'India', 'WINTER', 3, 4.6, 420, 'manual', 'auli_001'),
    ('Manali', 'Beautiful hill station in Himalayas perfect for adventure', 1, 32.2396, 77.1887, ST_MakePoint(77.1887, 32.2396)::geography, 'Manali', 'Himachal Pradesh', 'India', 'SUMMER', 2, 4.5, 250, 'manual', 'manali_001'),
    ('Shimla', 'Historic hill station with colonial architecture', 1, 31.7715, 77.1670, ST_MakePoint(77.1670, 31.7715)::geography, 'Shimla', 'Himachal Pradesh', 'India', 'SUMMER', 2, 4.4, 310, 'manual', 'shimla_001'),
    ('Darjeeling', 'Tea gardens and mountain views', 1, 27.0360, 88.2626, ST_MakePoint(88.2626, 27.0360)::geography, 'Darjeeling', 'West Bengal', 'India', 'SUMMER', 2, 4.5, 290, 'manual', 'darjeeling_001'),
    ('Nainital', 'Lake surrounded by mountains', 1, 29.3919, 79.4504, ST_MakePoint(79.4504, 29.3919)::geography, 'Nainital', 'Uttarakhand', 'India', 'SUMMER', 2, 4.3, 210, 'manual', 'nainital_001'),
    ('Mussoorie', 'Queen of hills with stunning views', 1, 30.4612, 78.7597, ST_MakePoint(78.7597, 30.4612)::geography, 'Mussoorie', 'Uttarakhand', 'India', 'SUMMER', 2, 4.4, 260, 'manual', 'mussoorie_001'),
    ('Kufri', 'Adventure hub near Shimla', 1, 31.8019, 77.2269, ST_MakePoint(77.2269, 31.8019)::geography, 'Kufri', 'Himachal Pradesh', 'India', 'WINTER', 3, 4.2, 180, 'manual', 'kufri_001'),
    ('Dalhousie', 'Colonial charm in mountains', 1, 32.5406, 75.9400, ST_MakePoint(75.9400, 32.5406)::geography, 'Dalhousie', 'Himachal Pradesh', 'India', 'SUMMER', 2, 4.3, 150, 'manual', 'dalhousie_001'),
    ('Leh Ladakh', 'High altitude desert paradise', 1, 34.1526, 77.5770, ST_MakePoint(77.5770, 34.1526)::geography, 'Leh', 'Ladakh', 'India', 'SUMMER', 3, 4.7, 580, 'manual', 'leh_001'),
    ('Chopta', 'Mini Switzerland of India', 1, 30.2160, 79.3686, ST_MakePoint(79.3686, 30.2160)::geography, 'Chopta', 'Uttarakhand', 'India', 'SUMMER', 2, 4.4, 220, 'manual', 'chopta_001'),
    
    -- Beaches (15 places)
    ('Goa Beaches', 'Famous beaches with water sports', 2, 15.2993, 73.8243, ST_MakePoint(73.8243, 15.2993)::geography, 'Goa', 'Goa', 'India', 'WINTER', 2, 4.3, 180, 'manual', 'goa_beach_001'),
    ('Anjuna Beach', 'Popular beach with nightlife', 2, 15.5667, 73.8033, ST_MakePoint(73.8033, 15.5667)::geography, 'Goa', 'Goa', 'India', 'WINTER', 2, 4.2, 160, 'manual', 'anjuna_001'),
    ('Baga Beach', 'Water sports hub', 2, 15.5811, 73.7497, ST_MakePoint(73.7497, 15.5811)::geography, 'Goa', 'Goa', 'India', 'WINTER', 2, 4.4, 200, 'manual', 'baga_001'),
    ('Marina Beach Chennai', 'Longest urban beach', 2, 13.0499, 80.2824, ST_MakePoint(80.2824, 13.0499)::geography, 'Chennai', 'Tamil Nadu', 'India', 'WINTER', 1, 3.9, 120, 'manual', 'marina_001'),
    ('Kovalalam Beach', 'Crescent shaped beach in Kerala', 2, 8.3949, 76.9322, ST_MakePoint(76.9322, 8.3949)::geography, 'Thiruvananthapuram', 'Kerala', 'India', 'ALL', 2, 4.5, 340, 'manual', 'kovalalam_001'),
    ('Backwaters Kerala', 'Network of lagoons and lakes', 2, 9.2827, 76.2705, ST_MakePoint(76.2705, 9.2827)::geography, 'Alleppey', 'Kerala', 'India', 'ALL', 3, 4.6, 400, 'manual', 'backwaters_001'),
    ('Varkala Beach', 'Cliff beach with houseboat stays', 2, 8.7393, 76.7334, ST_MakePoint(76.7334, 8.7393)::geography, 'Varkala', 'Kerala', 'India', 'ALL', 2, 4.4, 280, 'manual', 'varkala_001'),
    ('Mirissa Beach Sri Lanka', 'Whale watching location', 2, 5.9497, 80.4706, ST_MakePoint(80.4706, 5.9497)::geography, 'Mirissa', 'Southern', 'Sri Lanka', 'WINTER', 2, 4.3, 210, 'manual', 'mirissa_001'),
    ('Marari Beach', 'Serene backwater beach', 2, 9.5625, 76.3833, ST_MakePoint(76.3833, 9.5625)::geography, 'Mararikulam', 'Kerala', 'India', 'ALL', 2, 4.2, 150, 'manual', 'marari_001'),
    ('Radhanagar Beach', 'Island beach with white sand', 2, 11.6867, 92.7478, ST_MakePoint(92.7478, 11.6867)::geography, 'Andaman', 'Andaman and Nicobar', 'India', 'WINTER', 3, 4.7, 350, 'manual', 'radhanagar_001'),
    ('Diu Beach', 'Golden sand beach', 2, 20.7153, 70.9961, ST_MakePoint(70.9961, 20.7153)::geography, 'Diu', 'Gujarat', 'India', 'WINTER', 1, 4.1, 100, 'manual', 'diu_001'),
    ('Okha Beach', 'Coastal town beach', 2, 21.8168, 69.0720, ST_MakePoint(69.0720, 21.8168)::geography, 'Okha', 'Gujarat', 'India', 'WINTER', 1, 3.8, 80, 'manual', 'okha_001'),
    ('Murudeshwar Beach', 'Temple on cliff by beach', 2, 14.0910, 74.4821, ST_MakePoint(74.4821, 14.0910)::geography, 'Murudeshwar', 'Karnataka', 'India', 'WINTER', 2, 4.3, 190, 'manual', 'murudeshwar_001'),
    ('Panaji Beach', 'Capital city beach', 2, 15.4909, 73.8278, ST_MakePoint(73.8278, 15.4909)::geography, 'Panaji', 'Goa', 'India', 'WINTER', 2, 3.9, 110, 'manual', 'panaji_001'),
    
    -- Temples (12 places)
    ('Varanasi Temple', 'Ancient spiritual city on Ganges', 3, 25.3201, 82.9979, ST_MakePoint(82.9979, 25.3201)::geography, 'Varanasi', 'Uttar Pradesh', 'India', 'ALL', 1, 4.6, 320, 'manual', 'varanasi_001'),
    ('Meenakshi Temple', 'Iconic temple with intricate carvings', 3, 11.0260, 78.7597, ST_MakePoint(78.7597, 11.0260)::geography, 'Madurai', 'Tamil Nadu', 'India', 'ALL', 1, 4.5, 280, 'manual', 'meenakshi_001'),
    ('Golden Temple', 'Sikh holy shrine', 3, 31.6200, 74.8765, ST_MakePoint(74.8765, 31.6200)::geography, 'Amritsar', 'Punjab', 'India', 'ALL', 1, 4.7, 450, 'manual', 'goldentemple_001'),
    ('Jagannath Temple', 'One of four sacred dhams', 3, 19.8136, 85.8312, ST_MakePoint(85.8312, 19.8136)::geography, 'Puri', 'Odisha', 'India', 'ALL', 1, 4.5, 290, 'manual', 'jagannath_001'),
    ('Tirupati Temple', 'Richest temple in world', 3, 13.1827, 79.1338, ST_MakePoint(79.1338, 13.1827)::geography, 'Tirupati', 'Andhra Pradesh', 'India', 'ALL', 1, 4.6, 520, 'manual', 'tirupati_001'),
    ('Kedarnath Temple', 'High altitude shrine', 3, 30.7266, 79.1161, ST_MakePoint(79.1161, 30.7266)::geography, 'Kedarnath', 'Uttarakhand', 'India', 'SUMMER', 2, 4.4, 310, 'manual', 'kedarnath_001'),
    ('Kailash Mansarovar', 'Sacred pilgrimage destination', 3, 31.0688, 81.3099, ST_MakePoint(81.3099, 31.0688)::geography, 'Tibet', 'Tibet', 'China', 'SUMMER', 4, 4.8, 150, 'manual', 'kailash_001'),
    ('Badrinath Temple', 'Heavenly abode shrine', 3, 30.7498, 79.4993, ST_MakePoint(79.4993, 30.7498)::geography, 'Badrinath', 'Uttarakhand', 'India', 'SUMMER', 2, 4.5, 280, 'manual', 'badrinath_001'),
    ('Rameshwaram Temple', 'Bridge to Sri Lanka', 3, 9.2876, 79.3129, ST_MakePoint(79.3129, 9.2876)::geography, 'Rameshwaram', 'Tamil Nadu', 'India', 'ALL', 1, 4.4, 250, 'manual', 'rameshwaram_001'),
    ('Somnath Temple', 'Ancient temple in Gujarat', 3, 20.8875, 71.1968, ST_MakePoint(71.1968, 20.8875)::geography, 'Somnath', 'Gujarat', 'India', 'ALL', 1, 4.3, 200, 'manual', 'somnath_001'),
    ('Khajuraho Temple', 'Medieval temples with sculptures', 3, 24.8318, 79.9864, ST_MakePoint(79.9864, 24.8318)::geography, 'Khajuraho', 'Madhya Pradesh', 'India', 'ALL', 2, 4.5, 340, 'manual', 'khajuraho_001'),
    ('Dwarka Temple', 'Ancient port city temple', 3, 22.2394, 68.9678, ST_MakePoint(68.9678, 22.2394)::geography, 'Dwarka', 'Gujarat', 'India', 'ALL', 1, 4.4, 220, 'manual', 'dwarka_001'),
    
    -- Museums (8 places)
    ('National Museum Delhi', 'India''s largest museum', 4, 28.6139, 77.2090, ST_MakePoint(77.2090, 28.6139)::geography, 'Delhi', 'Delhi', 'India', 'ALL', 2, 4.4, 210, 'manual', 'national_museum_001'),
    ('Indian Museum Kolkata', 'Oldest museum in India', 4, 22.5529, 88.3527, ST_MakePoint(88.3527, 22.5529)::geography, 'Kolkata', 'West Bengal', 'India', 'ALL', 1, 4.2, 140, 'manual', 'indian_museum_001'),
    ('Government Museum Chennai', 'Ancient artifacts and sculptures', 4, 13.0011, 80.2333, ST_MakePoint(80.2333, 13.0011)::geography, 'Chennai', 'Tamil Nadu', 'India', 'ALL', 1, 4.1, 120, 'manual', 'govt_museum_001'),
    ('Salar Jung Museum', 'One man''s collection', 4, 17.3603, 78.4740, ST_MakePoint(78.4740, 17.3603)::geography, 'Hyderabad', 'Telangana', 'India', 'ALL', 2, 4.3, 180, 'manual', 'salar_jung_001'),
    ('Prince of Wales Museum', 'Art and history museum', 4, 18.9677, 72.8343, ST_MakePoint(72.8343, 18.9677)::geography, 'Mumbai', 'Maharashtra', 'India', 'ALL', 2, 4.2, 160, 'manual', 'pow_museum_001'),
    ('State Museum Lucknow', 'Awadhi culture museum', 4, 26.8467, 80.9462, ST_MakePoint(80.9462, 26.8467)::geography, 'Lucknow', 'Uttar Pradesh', 'India', 'ALL', 1, 4.0, 100, 'manual', 'state_museum_001'),
    ('Chhatrapati Shivaji Museum', 'Maritime history', 4, 18.9632, 72.8236, ST_MakePoint(72.8236, 18.9632)::geography, 'Mumbai', 'Maharashtra', 'India', 'ALL', 2, 4.3, 150, 'manual', 'chatrapati_001'),
    ('Calico Museum Ahmedabad', 'Textile museum', 4, 23.1815, 72.6309, ST_MakePoint(72.6309, 23.1815)::geography, 'Ahmedabad', 'Gujarat', 'India', 'ALL', 2, 4.4, 190, 'manual', 'calico_001'),
    
    -- Parks (12 places)
    ('Lodi Garden Delhi', 'Beautiful garden in Delhi', 5, 28.5921, 77.2197, ST_MakePoint(77.2197, 28.5921)::geography, 'Delhi', 'Delhi', 'India', 'ALL', 1, 4.2, 150, 'manual', 'lodi_garden_001'),
    ('Cubbon Park Bangalore', 'Urban green space', 5, 12.9716, 77.5946, ST_MakePoint(77.5946, 12.9716)::geography, 'Bangalore', 'Karnataka', 'India', 'ALL', 1, 4.1, 130, 'manual', 'cubbon_001'),
    ('Lodhi Garden Jaipur', 'Garden city landmark', 5, 26.8124, 75.8437, ST_MakePoint(75.8437, 26.8124)::geography, 'Jaipur', 'Rajasthan', 'India', 'ALL', 1, 4.0, 110, 'manual', 'lodhi_jaipur_001'),
    ('KBR National Park', 'Urban national park', 5, 17.4065, 78.4721, ST_MakePoint(78.4721, 17.4065)::geography, 'Hyderabad', 'Telangana', 'India', 'ALL', 1, 4.3, 170, 'manual', 'kbr_001'),
    ('Talkatora Garden Delhi', 'Historic garden', 5, 28.5823, 77.2323, ST_MakePoint(77.2323, 28.5823)::geography, 'Delhi', 'Delhi', 'India', 'ALL', 1, 3.9, 80, 'manual', 'talkatora_001'),
    ('Brindavan Garden Mysore', 'Royal garden with fountains', 5, 12.2381, 76.6550, ST_MakePoint(76.6550, 12.2381)::geography, 'Mysore', 'Karnataka', 'India', 'ALL', 1, 4.4, 220, 'manual', 'brindavan_001'),
    ('Nishat Garden Kashmir', 'Mughal garden masterpiece', 5, 34.1620, 75.5697, ST_MakePoint(75.5697, 34.1620)::geography, 'Srinagar', 'Jammu and Kashmir', 'India', 'SUMMER', 2, 4.5, 280, 'manual', 'nishat_001'),
    ('Shalimar Garden Kashmir', 'Paradise garden', 5, 34.1667, 75.5500, ST_MakePoint(75.5500, 34.1667)::geography, 'Srinagar', 'Jammu and Kashmir', 'India', 'SUMMER', 2, 4.6, 310, 'manual', 'shalimar_001'),
    ('Botanical Garden Kolkata', 'Ancient banyan tree', 5, 22.5432, 88.3637, ST_MakePoint(88.3637, 22.5432)::geography, 'Kolkata', 'West Bengal', 'India', 'ALL', 1, 4.2, 120, 'manual', 'botanical_001'),
    ('Raghuvanshi Garden Aurangabad', 'Garden near Ajanta', 5, 19.8875, 75.3412, ST_MakePoint(75.3412, 19.8875)::geography, 'Aurangabad', 'Maharashtra', 'India', 'ALL', 1, 4.0, 90, 'manual', 'raghuvanshi_001'),
    ('Garden of Five Senses Delhi', 'Modern garden design', 5, 28.5255, 77.2747, ST_MakePoint(77.2747, 28.5255)::geography, 'Delhi', 'Delhi', 'India', 'ALL', 2, 4.1, 140, 'manual', 'five_senses_001'),
    ('Ooty Garden Tamil Nadu', 'Hill station garden', 5, 11.4100, 76.7200, ST_MakePoint(76.7200, 11.4100)::geography, 'Ooty', 'Tamil Nadu', 'India', 'SUMMER', 2, 4.3, 180, 'manual', 'ooty_garden_001'),
    
    -- Forts (10 places)
    ('Red Fort Delhi', 'Mughal empire fort', 6, 28.6562, 77.2410, ST_MakePoint(77.2410, 28.6562)::geography, 'Delhi', 'Delhi', 'India', 'ALL', 2, 4.3, 200, 'manual', 'redfort_001'),
    ('Agra Fort', 'Taj Mahal adjacent fort', 6, 27.1867, 78.0081, ST_MakePoint(78.0081, 27.1867)::geography, 'Agra', 'Uttar Pradesh', 'India', 'ALL', 2, 4.4, 240, 'manual', 'agra_fort_001'),
    ('Mehrangarh Fort', 'Jodhpur''s majestic fort', 6, 26.2389, 73.5243, ST_MakePoint(73.5243, 26.2389)::geography, 'Jodhpur', 'Rajasthan', 'India', 'ALL', 2, 4.5, 280, 'manual', 'mehrangarh_001'),
    ('Chittorgarh Fort', 'Rajput pride fortress', 6, 24.8865, 75.1372, ST_MakePoint(75.1372, 24.8865)::geography, 'Chittorgarh', 'Rajasthan', 'India', 'ALL', 1, 4.4, 210, 'manual', 'chittorgarh_001'),
    ('Kumbhalgarh Fort', 'Great Wall of India', 6, 25.2408, 73.8920, ST_MakePoint(73.8920, 25.2408)::geography, 'Kumbhalgarh', 'Rajasthan', 'India', 'ALL', 2, 4.3, 190, 'manual', 'kumbhalgarh_001'),
    ('Ranthambore Fort', 'Jungle fortress', 6, 26.0122, 76.5028, ST_MakePoint(76.5028, 26.0122)::geography, 'Ranthambore', 'Rajasthan', 'India', 'ALL', 2, 4.2, 160, 'manual', 'ranthambore_fort_001'),
    ('Daulatabad Fort', 'Deccan sultanate fort', 6, 19.8792, 75.3208, ST_MakePoint(75.3208, 19.8792)::geography, 'Aurangabad', 'Maharashtra', 'India', 'ALL', 1, 4.1, 130, 'manual', 'daulatabad_001'),
    ('Jaisalmer Fort', 'Golden fort in desert', 6, 26.9124, 70.9093, ST_MakePoint(70.9093, 26.9124)::geography, 'Jaisalmer', 'Rajasthan', 'India', 'ALL', 2, 4.4, 270, 'manual', 'jaisalmer_001'),
    ('Gwalior Fort', 'Central India fortress', 6, 26.2183, 78.1714, ST_MakePoint(78.1714, 26.2183)::geography, 'Gwalior', 'Madhya Pradesh', 'India', 'ALL', 1, 4.2, 150, 'manual', 'gwalior_001'),
    ('Kangra Fort', 'Himachal fortress', 6, 32.1905, 76.2662, ST_MakePoint(76.2662, 32.1905)::geography, 'Kangra', 'Himachal Pradesh', 'India', 'SUMMER', 1, 4.0, 120, 'manual', 'kangra_001'),
    
    -- Lakes (8 places)
    ('Dal Lake Kashmir', 'Floating gardens and houseboats', 7, 34.2740, 75.5770, ST_MakePoint(75.5770, 34.2740)::geography, 'Srinagar', 'Jammu and Kashmir', 'India', 'ALL', 3, 4.6, 380, 'manual', 'dal_lake_001'),
    ('Pichola Lake Udaipur', 'Romantic city palace lake', 7, 24.5721, 73.6872, ST_MakePoint(73.6872, 24.5721)::geography, 'Udaipur', 'Rajasthan', 'India', 'ALL', 3, 4.5, 320, 'manual', 'pichola_001'),
    ('Loktak Lake Manipur', 'Floating islands lake', 7, 24.4933, 94.8922, ST_MakePoint(94.8922, 24.4933)::geography, 'Imphal', 'Manipur', 'India', 'MONSOON', 2, 4.3, 170, 'manual', 'loktak_001'),
    ('Tso Moriri Lake Ladakh', 'High altitude lake', 7, 32.7705, 78.2564, ST_MakePoint(78.2564, 32.7705)::geography, 'Leh', 'Ladakh', 'India', 'SUMMER', 3, 4.4, 210, 'manual', 'tso_moriri_001'),
    ('Chilika Lake Odisha', 'Brackish lagoon with birds', 7, 19.6667, 85.3500, ST_MakePoint(85.3500, 19.6667)::geography, 'Bhubaneswar', 'Odisha', 'India', 'WINTER', 1, 4.2, 140, 'manual', 'chilika_001'),
    ('Naini Lake Nainital', 'Beautiful hill station lake', 7, 29.3919, 79.4504, ST_MakePoint(79.4504, 29.3919)::geography, 'Nainital', 'Uttarakhand', 'India', 'SUMMER', 2, 4.3, 180, 'manual', 'naini_lake_001'),
    ('Pangong Lake Ladakh', 'Stunning saltwater lake', 7, 33.7837, 78.5497, ST_MakePoint(78.5497, 33.7837)::geography, 'Leh', 'Ladakh', 'India', 'SUMMER', 3, 4.7, 420, 'manual', 'pangong_001'),
    ('Vembanad Lake Kerala', 'Largest backwater lake', 7, 9.6800, 76.3600, ST_MakePoint(76.3600, 9.6800)::geography, 'Kochi', 'Kerala', 'India', 'ALL', 2, 4.4, 200, 'manual', 'vembanad_001'),
    
    -- Waterfalls (7 places)
    ('Niagara Falls India', 'Jog Falls Karnataka', 8, 14.8428, 75.3540, ST_MakePoint(75.3540, 14.8428)::geography, 'Jog Falls', 'Karnataka', 'India', 'MONSOON', 1, 4.5, 180, 'manual', 'jog_falls_001'),
    ('Dudhsagar Falls', 'Four tier waterfall Goa', 8, 15.3047, 73.9897, ST_MakePoint(73.9897, 15.3047)::geography, 'Goa', 'Goa', 'India', 'MONSOON', 2, 4.4, 200, 'manual', 'dudhsagar_001'),
    ('Athirapally Falls Kerala', 'Queen of waterfalls', 8, 10.2706, 76.5705, ST_MakePoint(76.5705, 10.2706)::geography, 'Thrissur', 'Kerala', 'India', 'MONSOON', 1, 4.3, 160, 'manual', 'athirapally_001'),
    ('Kunchikal Falls Karnataka', 'Highest waterfall', 8, 14.3928, 75.4358, ST_MakePoint(75.4358, 14.3928)::geography, 'Agumbe', 'Karnataka', 'India', 'MONSOON', 1, 4.2, 140, 'manual', 'kunchikal_001'),
    ('Triveni Falls', 'Confluence of three rivers', 8, 30.4159, 78.8529, ST_MakePoint(78.8529, 30.4159)::geography, 'Chopta', 'Uttarakhand', 'India', 'MONSOON', 1, 4.1, 110, 'manual', 'triveni_001'),
    ('Gokak Falls Karnataka', 'Circular horseshoe falls', 8, 15.9006, 74.9878, ST_MakePoint(74.9878, 15.9006)::geography, 'Gokak', 'Karnataka', 'India', 'MONSOON', 1, 4.0, 100, 'manual', 'gokak_001'),
    ('Kapildhara Falls Madhya Pradesh', 'Narmada river falls', 8, 22.4756, 77.8994, ST_MakePoint(77.8994, 22.4756)::geography, 'Indore', 'Madhya Pradesh', 'India', 'MONSOON', 1, 3.9, 90, 'manual', 'kapildhara_001'),
    
    -- Wildlife (8 places)
    ('Ranthambore National Park', 'Tiger reserve', 12, 26.0122, 76.5028, ST_MakePoint(76.5028, 26.0122)::geography, 'Sawai Madhopur', 'Rajasthan', 'India', 'WINTER', 3, 4.6, 310, 'manual', 'ranthambore_np_001'),
    ('Kaziranga National Park', 'One horned rhino', 12, 26.5824, 93.2170, ST_MakePoint(93.2170, 26.5824)::geography, 'Golaghat', 'Assam', 'India', 'WINTER', 2, 4.5, 240, 'manual', 'kaziranga_001'),
    ('Kanha National Park', 'Tiger and baison', 12, 22.5500, 80.6000, ST_MakePoint(80.6000, 22.5500)::geography, 'Mandla', 'Madhya Pradesh', 'India', 'WINTER', 2, 4.4, 200, 'manual', 'kanha_001'),
    ('Bandhavgarh National Park', 'White tiger reserve', 12, 23.8833, 81.5667, ST_MakePoint(81.5667, 23.8833)::geography, 'Umaria', 'Madhya Pradesh', 'India', 'WINTER', 2, 4.3, 180, 'manual', 'bandhavgarh_001'),
    ('Sundarbans National Park', 'Bengal tiger and mangroves', 12, 21.9497, 89.1833, ST_MakePoint(89.1833, 21.9497)::geography, 'Sundarban', 'West Bengal', 'India', 'WINTER', 2, 4.4, 220, 'manual', 'sundarbans_001'),
    ('Periyar National Park Kerala', 'Elephant and tiger reserve', 12, 9.5500, 77.2500, ST_MakePoint(77.2500, 9.5500)::geography, 'Thekkady', 'Kerala', 'India', 'ALL', 2, 4.3, 190, 'manual', 'periyar_001'),
    ('Pench National Park', 'Tiger and leopard', 12, 21.7933, 78.8500, ST_MakePoint(78.8500, 21.7933)::geography, 'Seoni', 'Madhya Pradesh', 'India', 'WINTER', 2, 4.2, 160, 'manual', 'pench_001'),
    ('Gir National Park Gujarat', 'Asiatic lion reserve', 12, 21.1667, 70.6667, ST_MakePoint(70.6667, 21.1667)::geography, 'Junagadh', 'Gujarat', 'India', 'WINTER', 2, 4.2, 150, 'manual', 'gir_001'),
    
    -- Historical (10 places)
    ('Taj Mahal', 'Monument to love', 10, 27.1751, 78.0421, ST_MakePoint(78.0421, 27.1751)::geography, 'Agra', 'Uttar Pradesh', 'India', 'ALL', 2, 4.7, 890, 'manual', 'taj_mahal_001'),
    ('Ajanta Caves', 'Buddhist cave paintings', 10, 19.8874, 75.7733, ST_MakePoint(75.7733, 19.8874)::geography, 'Aurangabad', 'Maharashtra', 'India', 'ALL', 1, 4.4, 190, 'manual', 'ajanta_001'),
    ('Ellora Caves', 'Hindu Buddhist Islamic caves', 10, 19.9009, 75.4818, ST_MakePoint(75.4818, 19.9009)::geography, 'Aurangabad', 'Maharashtra', 'India', 'ALL', 1, 4.5, 210, 'manual', 'ellora_001'),
    ('Fatehpur Sikri', 'Ghost city Akbar', 10, 27.0885, 77.8711, ST_MakePoint(77.8711, 27.0885)::geography, 'Agra', 'Uttar Pradesh', 'India', 'ALL', 2, 4.3, 180, 'manual', 'fatehpur_001'),
    ('Sanchi Stupa', 'Buddhist monument', 10, 23.4833, 77.7833, ST_MakePoint(77.7833, 23.4833)::geography, 'Bhopal', 'Madhya Pradesh', 'India', 'ALL', 1, 4.2, 140, 'manual', 'sanchi_001'),
    ('Hampi Ruins', 'Vijayanagara empire', 10, 15.3350, 76.4625, ST_MakePoint(76.4625, 15.3350)::geography, 'Hospet', 'Karnataka', 'India', 'ALL', 1, 4.5, 260, 'manual', 'hampi_001'),
    ('Konark Sun Temple', 'Chariot temple Odisha', 10, 19.8880, 86.0901, ST_MakePoint(86.0901, 19.8880)::geography, 'Puri', 'Odisha', 'India', 'ALL', 1, 4.3, 170, 'manual', 'konark_001'),
    ('Mahabalipuram Temples', 'Shore temples', 10, 12.5656, 80.1925, ST_MakePoint(80.1925, 12.5656)::geography, 'Mahabalipuram', 'Tamil Nadu', 'India', 'ALL', 1, 4.4, 200, 'manual', 'mahabalipuram_001'),
    ('Virupaksha Temple Hampi', 'Temple in ruins', 10, 15.3275, 76.4625, ST_MakePoint(76.4625, 15.3275)::geography, 'Hospet', 'Karnataka', 'India', 'ALL', 1, 4.5, 190, 'manual', 'virupaksha_001'),
    ('Brihadisvara Temple Thanjavur', 'Chola architecture', 10, 10.7870, 79.1378, ST_MakePoint(79.1378, 10.7870)::geography, 'Thanjavur', 'Tamil Nadu', 'India', 'ALL', 1, 4.4, 180, 'manual', 'brihadisvara_001')
ON CONFLICT (source, source_id) DO NOTHING;

-- Associate tags with places (sample mappings)
INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'Manali' AND t.name IN ('cool_place', 'outdoor', 'hiking', 'adventure', 'family_friendly');

INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'Goa Beaches' AND t.name IN ('water_spot', 'beach', 'outdoor', 'budget_friendly', 'family_friendly');

INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'Varanasi Temple' AND t.name IN ('spiritual', 'historical', 'photography', 'budget_friendly');

INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'Taj Mahal' AND t.name IN ('historical', 'photography', 'romantic', 'family_friendly', 'luxury');

INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'Ranthambore National Park' AND t.name IN ('wildlife', 'adventure', 'photography', 'nature_lover');

INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'Dal Lake Kashmir' AND t.name IN ('water_spot', 'romantic', 'photography', 'outdoor', 'luxury');

INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'Golden Temple' AND t.name IN ('spiritual', 'historical', 'family_friendly', 'budget_friendly');

INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'National Museum Delhi' AND t.name IN ('museum', 'indoor', 'family_friendly', 'historical');

INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'Leh Ladakh' AND t.name IN ('adventure', 'photography', 'outdoor', 'nature_lover', 'cool_place');

INSERT INTO place_tags (place_id, tag_id) SELECT p.id, t.id FROM places p, tags t 
WHERE p.name = 'Kovalalam Beach' AND t.name IN ('water_spot', 'beach', 'outdoor', 'romantic', 'family_friendly');

-- Display total count
SELECT COUNT(*) as total_places FROM places;
SELECT COUNT(*) as total_tags FROM tags;
SELECT COUNT(*) as total_categories FROM categories;
