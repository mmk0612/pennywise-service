DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'budgets'
  ) THEN
    ALTER TABLE budgets ADD COLUMN IF NOT EXISTS "billingMonth" varchar(255);
    UPDATE budgets
    SET "billingMonth" = COALESCE(
      "billingMonth",
      to_char(date_trunc('month', COALESCE("createdAt", now())), 'YYYY-MM')
    );
    ALTER TABLE budgets ALTER COLUMN "billingMonth" SET NOT NULL;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'expenses'
  ) THEN
    ALTER TABLE expenses ADD COLUMN IF NOT EXISTS "billingMonth" varchar(255);
    UPDATE expenses
    SET "billingMonth" = COALESCE(
      "billingMonth",
      to_char(date_trunc('month', COALESCE("expenseDate", now())), 'YYYY-MM')
    );
    ALTER TABLE expenses ALTER COLUMN "billingMonth" SET NOT NULL;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'budget_monthly_archives'
  ) THEN
    ALTER TABLE budget_monthly_archives ADD COLUMN IF NOT EXISTS "billingMonth" varchar(255);
    UPDATE budget_monthly_archives
    SET "billingMonth" = COALESCE(
      "billingMonth",
      to_char(date_trunc('month', COALESCE("archivedAt", now())), 'YYYY-MM')
    );
    ALTER TABLE budget_monthly_archives ALTER COLUMN "billingMonth" SET NOT NULL;
  END IF;

  IF EXISTS (
    SELECT 1
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_name = 'expense_monthly_archives'
  ) THEN
    ALTER TABLE expense_monthly_archives ADD COLUMN IF NOT EXISTS "billingMonth" varchar(255);
    UPDATE expense_monthly_archives
    SET "billingMonth" = COALESCE(
      "billingMonth",
      to_char(date_trunc('month', COALESCE("archivedAt", now())), 'YYYY-MM')
    );
    ALTER TABLE expense_monthly_archives ALTER COLUMN "billingMonth" SET NOT NULL;
  END IF;
END $$;