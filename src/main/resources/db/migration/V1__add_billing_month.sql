DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'budgets'
  ) THEN
    ALTER TABLE budgets ADD COLUMN IF NOT EXISTS billing_month varchar(255);
    UPDATE budgets
    SET billing_month = COALESCE(
      billing_month,
      to_char(date_trunc('month', COALESCE(created_at, now())), 'YYYY-MM')
    );
    ALTER TABLE budgets ALTER COLUMN billing_month SET NOT NULL;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'expenses'
  ) THEN
    ALTER TABLE expenses ADD COLUMN IF NOT EXISTS billing_month varchar(255);
    UPDATE expenses
    SET billing_month = COALESCE(
      billing_month,
      to_char(date_trunc('month', COALESCE(expense_date, now())), 'YYYY-MM')
    );
    ALTER TABLE expenses ALTER COLUMN billing_month SET NOT NULL;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'budget_monthly_archives'
  ) THEN
    ALTER TABLE budget_monthly_archives ADD COLUMN IF NOT EXISTS billing_month varchar(255);
    UPDATE budget_monthly_archives
    SET billing_month = COALESCE(
      billing_month,
      to_char(date_trunc('month', COALESCE(archived_at, now())), 'YYYY-MM')
    );
    ALTER TABLE budget_monthly_archives ALTER COLUMN billing_month SET NOT NULL;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'expense_monthly_archives'
  ) THEN
    ALTER TABLE expense_monthly_archives ADD COLUMN IF NOT EXISTS billing_month varchar(255);
    UPDATE expense_monthly_archives
    SET billing_month = COALESCE(
      billing_month,
      to_char(date_trunc('month', COALESCE(archived_at, now())), 'YYYY-MM')
    );
    ALTER TABLE expense_monthly_archives ALTER COLUMN billing_month SET NOT NULL;
  END IF;
END $$;