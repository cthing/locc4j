(*
  File:     Chebyshev_Polynomials/Chebyshev_Polynomials.thy
  Author:   Manuel Eberl (University of Innsbruck)
*)
section ‹Chebyshev Polynomials›
theory Chebyshev_Polynomials
imports
  "HOL-Analysis.Analysis"
  "HOL-Real_Asymp.Real_Asymp"
  "HOL-Computational_Algebra.Formal_Laurent_Series"
  "Polynomial_Interpolation.Ring_Hom_Poly"
  "Descartes_Sign_Rule.Descartes_Sign_Rule"
  Polynomial_Transfer
  Chebyshev_Polynomials_Library
begin


subsection ‹Definition›

text ‹
\definecolor{mycol1}{HTML}{fd7f6f}
\definecolor{mycol2}{HTML}{7eb0d5}
\definecolor{mycol3}{HTML}{b2e061}
\definecolor{mycol4}{HTML}{bd7ebe}
\definecolor{mycol5}{HTML}{ffb55a}
\definecolor{mycol6}{HTML}{ffee65}
\definecolor{mycol7}{HTML}{beb9db}
\definecolor{mycol8}{HTML}{fdcce5}
\definecolor{mycol9}{HTML}{8bd3c7}
›

text ‹
  We choose the recursive definition of $T_n$ and $U_n$ and do some setup to define
  both of them at once.
›

locale gen_cheb_poly =
  fixes c :: "'a :: comm_ring_1"
begin

fun f :: "nat ⇒ 'a ⇒ 'a" where
  "f 0 x = 1"
| "f (Suc 0) x = c * x"
| "f (Suc (Suc n )) x = 2 * x * f (Suc n) x - f n x"

fun P :: "nat ⇒ ('a :: comm_ring_1) poly" where
  "P 0 = 1"
| "P (Suc 0) = [:0, c:]"
| "P (Suc (Suc n)) = [:0, 2:] * P (Suc n) - P n"

lemma eval [simp]: "poly (P n) x = f n x"
  by (induction n rule: P.induct) simp_all

lemma eval_0:
  "f n 0 = (if odd n then 0 else (-1) ^ (n div 2))"
  by (induction n rule: induct_nat_012) auto

lemma eval_1 [simp]:
  "f n 1 = of_nat n * (c - 1) + 1"
proof (induction n rule: induct_nat_012)
  case (ge2 n)
  show ?case
    by (auto simp: ge2.IH algebra_simps)
qed auto

lemma uminus [simp]: "f n (-x) = (-1) ^ n  * f n x"
  by (induction n rule: P.induct) (simp_all add: algebra_simps)

lemma pcompose_minus: "pcompose (P n) (monom (-1) 1) = (-1) ^ n * P n"
  by (induction n rule: induct_nat_012)
     (simp_all add: pcompose_diff pcompose_uminus pcompose_smult one_pCons
                    poly_const_pow algebra_simps monom_altdef)

lemma degree_le: "degree (P n) ≤ n"
proof -
  have "i > n ⟹ coeff (P n) i = 0" for i
  by (induction n arbitrary: i rule: P.induct)
     (auto simp: coeff_pCons split: nat.splits)
  thus ?thesis
    using degree_le by blast
qed

lemma lead_coeff:
  "coeff (P n) n = (if n = 0 then 1 else c * 2 ^ (n - 1))"
proof (induction n rule: P.induct)
  case (3 n)
  thus ?case
    using degree_le[of n] by (auto simp: coeff_eq_0 algebra_simps)
qed auto

lemma degree_eq:
  "c * 2 ^ (n - 1) ≠ 0 ⟹ degree (P n :: 'a poly) = n"
  using lead_coeff[of n] degree_le[of n]
  by (metis le_degree nle_le one_neq_zero)

lemmas [simp del] = f.simps(3) P.simps(3)

end


text ‹
  The two related constants ‹Cheb_poly› and ‹cheb_poly› denote the ‹n›-th Chebyshev
  polynomial of the first kind $T_n$ and its interpretation as a function. We make the
  definition polymorphic so that it works on every commutative ring; however, many
  results will only hold for rings (or even only fields) of characteristic ‹0›.
›
definition cheb_poly :: "nat ⇒ 'a :: comm_ring_1 ⇒ 'a" where
 "cheb_poly = gen_cheb_poly.f 1"

definition Cheb_poly :: "nat ⇒ 'a :: comm_ring_1 poly" where
 "Cheb_poly = gen_cheb_poly.P 1"

interpretation cheb_poly: gen_cheb_poly 1
  rewrites "gen_cheb_poly.f 1 ≡ cheb_poly" and "gen_cheb_poly.P 1 = Cheb_poly"
       and "⋀x :: 'a. 1 * x = x"
       and "⋀n. of_nat n * (1 - 1 :: 'a) + 1 = 1"
  by unfold_locales (simp_all add: cheb_poly_def Cheb_poly_def)

lemmas cheb_poly_simps [code] = cheb_poly.f.simps
lemmas Cheb_poly_simps [code] = cheb_poly.P.simps

qed (simp_all add: cheb_eval_def)

end
