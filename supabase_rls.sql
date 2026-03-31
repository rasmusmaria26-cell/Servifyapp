-- Enable Row Level Security
ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE bookings ENABLE ROW LEVEL SECURITY;
ALTER TABLE vendors ENABLE ROW LEVEL SECURITY;

-- Profiles Policies
CREATE POLICY "Users can view own profile" 
ON profiles FOR SELECT 
USING (auth.uid() = user_id);

CREATE POLICY "Users can update own profile" 
ON profiles FOR UPDATE 
USING (auth.uid() = user_id);

-- User Roles Policies
CREATE POLICY "Users can view own role" 
ON user_roles FOR SELECT 
USING (auth.uid() = user_id);

-- Bookings Policies
CREATE POLICY "Customers can view own bookings" 
ON bookings FOR SELECT 
USING (auth.uid() = customer_id);

CREATE POLICY "Customers can insert own bookings" 
ON bookings FOR INSERT 
WITH CHECK (auth.uid() = customer_id);

CREATE POLICY "Vendors can view assigned bookings" 
ON bookings FOR SELECT 
USING (auth.uid() = vendor_id);

CREATE POLICY "Vendors can update assigned bookings" 
ON bookings FOR UPDATE 
USING (auth.uid() = vendor_id);

-- Vendors Policies
CREATE POLICY "Public can view verified vendors" 
ON vendors FOR SELECT 
USING (is_verified = true);

CREATE POLICY "Vendors can update own profile" 
ON vendors FOR UPDATE 
USING (auth.uid() = id);
